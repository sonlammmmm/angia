package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.common.enums.PaymentMethod;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.common.enums.PaymentStatus;
import vn.dichvuangia.management.dto.request.OrderCreateRequest;
import vn.dichvuangia.management.dto.request.OrderStatusUpdateRequest;
import vn.dichvuangia.management.dto.request.GuestOrderCreateRequest;
import vn.dichvuangia.management.dto.response.OrderItemResponse;
import vn.dichvuangia.management.dto.response.OrderResponse;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.Order;
import vn.dichvuangia.management.entity.OrderItem;
import vn.dichvuangia.management.entity.Payment;
import vn.dichvuangia.management.entity.Product;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.InsufficientStockException;
import vn.dichvuangia.management.exception.InvalidStatusTransitionException;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.OrderRepository;
import vn.dichvuangia.management.repository.PaymentRepository;
import vn.dichvuangia.management.repository.ProductRepository;
import vn.dichvuangia.management.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OrderService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Random RANDOM = new Random();

    // COMPLETED/CANCELLED là trạng thái kết thúc, không cho phép chuyển tiếp.
    private static final Set<OrderStatus> TERMINAL = Set.of(OrderStatus.CANCELLED, OrderStatus.COMPLETED);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAll(OrderStatus status, Long customerId, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        String scope = getCurrentScope();

        // SALE: chỉ thấy đơn của mình
        if ("ROLE_SALE".equals(scope)) {
            Long currentUserId = getCurrentUserId();
            return orderRepository.findAllWithFilter(status, currentUserId, customerId, from, to, pageable)
                    .map(this::toResponseWithPayment);
        }

        // ADMIN / MANAGEMENT: thấy tất cả
        return orderRepository.findAllWithFilter(status, null, customerId, from, to, pageable)
                .map(this::toResponseWithPayment);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toResponseWithPayment(findOrderById(id));
    }

    /**
     * Tạo đơn hàng (Staff/Sale):
     * <p>
     * Flow khách hàng:
     * 1. Nếu có customerId → dùng Customer đã tồn tại.
     * 2. Nếu không có customerId → tìm theo phone:
     *    - Nếu tìm thấy → dùng Customer đó (cập nhật fullName/email/address nếu thay đổi).
     *    - Nếu không tìm thấy → tạo mới Customer với phone + fullName.
     * <p>
     * Validate tồn kho, sinh order_code, snapshot unit_price.
     */
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Long currentUserId = getCurrentUserId();
        User sale = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

        Customer customer;
        if (request.getCustomerId() != null) {
            // Chọn khách hàng đã đăng ký
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));
        } else {
            // Tìm hoặc tạo khách hàng theo SĐT
            if (request.getPhone() == null || request.getPhone().isBlank()) {
                throw new IllegalArgumentException("Số điện thoại không được để trống khi không chọn khách hàng");
            }
            if (request.getFullName() == null || request.getFullName().isBlank()) {
                throw new IllegalArgumentException("Họ tên không được để trống khi không chọn khách hàng");
            }
            customer = findOrCreateCustomerByPhone(request, sale);
        }

        // Validate stock trước
        List<Product> products = request.getItems().stream()
                .map(item -> {
                    Product p = productRepository.findByIdAndIsDeletedFalse(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));
                    if (p.getStockQuantity() < item.getQuantity()) {
                        throw new InsufficientStockException(p.getName(), p.getStockQuantity());
                    }
                    return p;
                })
                .toList();

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setSale(sale);
        order.setShippingAddress(request.getShippingAddress());
    order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);

        // Tạo order items + tính total
        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < request.getItems().size(); i++) {
            var itemReq = request.getItems().get(i);
            Product product = products.get(i);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice()); // snapshot tại thời điểm mua
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        createFreePaymentIfNeeded(saved);
        createCashPaymentIfNeeded(saved, request.getPaymentMethod());
        return toResponseWithPayment(saved);
    }

    /**
     * Khách vãng lai đặt hàng — không cần đăng nhập.
     * Tìm Customer theo số điện thoại, nếu chưa có thì tạo mới.
     * sale = null (đơn online — không có nhân viên chốt).
     */
    @Transactional
    public OrderResponse createGuest(GuestOrderCreateRequest request) {
        Customer customer = findOrCreateGuestCustomer(
                request.getFullName(), request.getPhone(), request.getShippingAddress());

        // Validate stock trước
        List<Product> products = request.getItems().stream()
                .map(item -> {
                    Product p = productRepository.findByIdAndIsDeletedFalse(item.getProductId())
                            .orElseThrow(() -> new ResourceNotFoundException("Product", item.getProductId()));
                    if (p.getStockQuantity() < item.getQuantity()) {
                        throw new InsufficientStockException(p.getName(), p.getStockQuantity());
                    }
                    return p;
                })
                .toList();

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setCustomer(customer);
        order.setSale(null); // đơn online — không có nhân viên chốt
        order.setShippingAddress(request.getShippingAddress());
    order.setNotes(request.getNotes());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal total = BigDecimal.ZERO;
        for (int i = 0; i < request.getItems().size(); i++) {
            var itemReq = request.getItems().get(i);
            Product product = products.get(i);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantity(itemReq.getQuantity());
            item.setUnitPrice(product.getPrice());
            order.getItems().add(item);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalAmount(total);
        Order saved = orderRepository.save(order);
        createFreePaymentIfNeeded(saved);
        createCashPaymentIfNeeded(saved, request.getPaymentMethod());
        return toResponseWithPayment(saved);
    }

    /**
     * Cập nhật trạng thái đơn hàng theo state machine:
     * - PENDING → PROCESSING | CANCELLED
     * - PROCESSING → COMPLETED (trừ stock) | CANCELLED (hoàn stock nếu cần)
     * - COMPLETED / CANCELLED → không đổi được
     */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatusUpdateRequest request) {
        Order order = findOrderById(id);
        OrderStatus current = order.getStatus();
        OrderStatus next = request.getStatus();

        validateTransition(current, next);

        if (next == OrderStatus.COMPLETED) {
            // Trừ tồn kho khi hoàn thành đơn
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                int newStock = product.getStockQuantity() - item.getQuantity();
                if (newStock < 0) {
                    throw new InsufficientStockException(product.getName(), product.getStockQuantity());
                }
                product.setStockQuantity(newStock);
                productRepository.save(product);
            }
        }

        order.setStatus(next);
        return toResponseWithPayment(orderRepository.save(order));
    }

    /**
     * Cập nhật ghi chú đơn hàng.
     */
    @Transactional
    public OrderResponse updateNotes(Long id, String notes) {
        Order order = findOrderById(id);
        order.setNotes(notes);
        return toResponseWithPayment(orderRepository.save(order));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (TERMINAL.contains(from)) {
            throw new InvalidStatusTransitionException(from.name(), to.name());
        }
        boolean valid = switch (from) {
            case PENDING    -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.COMPLETED  || to == OrderStatus.CANCELLED;
            default         -> false;
        };
        if (!valid) {
            throw new InvalidStatusTransitionException(from.name(), to.name());
        }
    }

    /**
     * Sinh ORD-{yyyyMMdd}-{5digits} — kiểm tra trùng, thử lại tối đa 5 lần.
     */
    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DATE_FMT);
        for (int attempt = 0; attempt < 5; attempt++) {
            String code = String.format("ORD-%s-%05d", date, RANDOM.nextInt(100000));
            if (!orderRepository.existsByOrderCode(code)) {
                return code;
            }
        }
        // Fallback: timestamp millis
        return "ORD-" + date + "-" + System.currentTimeMillis() % 100000;
    }

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }

    /**
     * Tìm Customer theo số điện thoại, nếu chưa có thì tạo mới (khách vãng lai).
     */
    private Customer findOrCreateGuestCustomer(String fullName, String phone, String address) {
        return customerRepository.findByPhone(phone)
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFullName(fullName);
                    c.setPhone(phone);
                    c.setAddress(address);
                    return customerRepository.save(c);
                });
    }

    /**
     * Tìm Customer theo SĐT, nếu tìm thấy thì cập nhật thông tin (nếu thay đổi),
     * nếu chưa có thì tạo mới — gắn createdBy = Sale hiện tại.
     */
    private Customer findOrCreateCustomerByPhone(OrderCreateRequest request, User sale) {
        return customerRepository.findByPhone(request.getPhone())
                .map(existing -> {
                    // Cập nhật thông tin mới nhất nếu Sale cung cấp
                    if (request.getFullName() != null && !request.getFullName().isBlank()) {
                        existing.setFullName(request.getFullName());
                    }
                    if (request.getEmail() != null && !request.getEmail().isBlank()) {
                        existing.setEmail(request.getEmail());
                    }
                    if (request.getShippingAddress() != null && !request.getShippingAddress().isBlank()) {
                        existing.setAddress(request.getShippingAddress());
                    }
                    return customerRepository.save(existing);
                })
                .orElseGet(() -> {
                    Customer c = new Customer();
                    c.setFullName(request.getFullName());
                    c.setPhone(request.getPhone());
                    c.setEmail(request.getEmail());
                    c.setAddress(request.getShippingAddress());
                    c.setCreatedBy(sale);
                    return customerRepository.save(c);
                });
    }

    private Long getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("userId");
    }

    private String getCurrentScope() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("scope");
    }

    static OrderResponse toResponse(Order order) {
        return toResponse(order, null, null);
    }

    static OrderResponse toResponse(Order order, PaymentStatus paymentStatus, PaymentMethod paymentMethod) {
        List<OrderItemResponse> items = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .productCode(item.getProduct().getProductCode())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .status(order.getStatus())
                .paymentStatus(paymentStatus)
                .paymentMethod(paymentMethod)
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .customerId(order.getCustomer().getId())
                .customerName(order.getCustomer().getFullName())
                .customerPhone(order.getCustomer().getPhone())
                .saleId(order.getSale() != null ? order.getSale().getId() : null)
                .saleUsername(order.getSale() != null ? order.getSale().getUsername() : null)
                .items(items)
                .build();
    }

    private OrderResponse toResponseWithPayment(Order order) {
    var paymentOpt = paymentRepository
        .findTopByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(PaymentReferenceType.ORDER, order.getId());
    PaymentStatus ps = paymentOpt.map(Payment::getStatus).orElse(null);
    PaymentMethod pm = paymentOpt.map(Payment::getMethod).orElse(null);
    return toResponse(order, ps, pm);
    }

    /**
     * Cập nhật trạng thái thanh toán nhanh cho đơn hàng.
     * Nếu chưa có Payment record, tạo mới. Nếu đã có, cập nhật status.
     */
    @Transactional
    public OrderResponse updatePaymentStatus(Long id, PaymentStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", id));

        Payment payment = paymentRepository
                .findTopByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(PaymentReferenceType.ORDER, id)
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setReferenceType(PaymentReferenceType.ORDER);
                    p.setReferenceId(order.getId());
                    p.setReferenceCode(order.getOrderCode());
                    p.setAmountVnd(order.getTotalAmount());
                    p.setCurrency("VND");
                    p.setMethod(PaymentMethod.FREE);
                    return p;
                });

        payment.setStatus(newStatus);
        paymentRepository.save(payment);

        return toResponse(order, newStatus, payment.getMethod());
    }

    private void createFreePaymentIfNeeded(Order order) {
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) == 0) {
            Payment payment = new Payment();
            payment.setReferenceType(PaymentReferenceType.ORDER);
            payment.setReferenceId(order.getId());
            payment.setReferenceCode(order.getOrderCode());
            payment.setAmountVnd(BigDecimal.ZERO);
            payment.setAmountUsd(BigDecimal.ZERO);
            payment.setCurrency("VND");
            payment.setMethod(PaymentMethod.FREE);
            payment.setStatus(PaymentStatus.FREE);
            paymentRepository.save(payment);
        }
    }

    private void createCashPaymentIfNeeded(Order order, PaymentMethod paymentMethod) {
        if (paymentMethod != PaymentMethod.CASH) {
            return;
        }
        if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        Payment payment = new Payment();
        payment.setReferenceType(PaymentReferenceType.ORDER);
        payment.setReferenceId(order.getId());
        payment.setReferenceCode(order.getOrderCode());
        payment.setAmountVnd(order.getTotalAmount());
        payment.setAmountUsd(BigDecimal.ZERO);
        payment.setCurrency("VND");
        payment.setMethod(PaymentMethod.CASH);
        payment.setStatus(PaymentStatus.CREATED);
        paymentRepository.save(payment);
    }
}
