package vn.dichvuangia.management.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.dto.request.OrderCreateRequest;
import vn.dichvuangia.management.dto.request.OrderStatusUpdateRequest;
import vn.dichvuangia.management.dto.request.GuestOrderCreateRequest;
import vn.dichvuangia.management.dto.response.OrderItemResponse;
import vn.dichvuangia.management.dto.response.OrderResponse;
import vn.dichvuangia.management.entity.Customer;
import vn.dichvuangia.management.entity.Order;
import vn.dichvuangia.management.entity.OrderItem;
import vn.dichvuangia.management.entity.Product;
import vn.dichvuangia.management.entity.User;
import vn.dichvuangia.management.exception.InsufficientStockException;
import vn.dichvuangia.management.exception.InvalidStatusTransitionException;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.CustomerRepository;
import vn.dichvuangia.management.repository.OrderRepository;
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

    // CANCELLED là terminal duy nhất.
    // COMPLETED → CANCELLED được phép (hoàn đơn) — cộng lại stock.
    private static final Set<OrderStatus> TERMINAL = Set.of(OrderStatus.CANCELLED);

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAll(OrderStatus status, Long customerId, Pageable pageable) {
        String scope = getCurrentScope();

        // SALE: chỉ thấy đơn của mình
        if ("ROLE_SALE".equals(scope)) {
            Long currentUserId = getCurrentUserId();
            return orderRepository.findAllWithFilter(status, currentUserId, customerId, pageable)
                    .map(OrderService::toResponse);
        }

        // ADMIN / MANAGEMENT: thấy tất cả
        return orderRepository.findAllWithFilter(status, null, customerId, pageable)
                .map(OrderService::toResponse);
    }

    @Transactional(readOnly = true)
    public OrderResponse getById(Long id) {
        return toResponse(findOrderById(id));
    }

    /**
     * Tạo đơn hàng:
     * 1. Validate tồn kho (không trừ ngay — chỉ trừ khi COMPLETED)
     * 2. Sinh order_code = ORD-{yyyyMMdd}-{5digits}
     * 3. Snapshot unit_price từ product.price tại thời điểm mua
     * 4. Tính total_amount
     */
    @Transactional
    public OrderResponse create(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", request.getCustomerId()));

        Long currentUserId = getCurrentUserId();
        User sale = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", currentUserId));

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
        return toResponse(orderRepository.save(order));
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
        return toResponse(orderRepository.save(order));
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

        if (next == OrderStatus.CANCELLED && current == OrderStatus.COMPLETED) {
            // Hoàn đơn: cộng lại tồn kho
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productRepository.save(product);
            }
        }

        order.setStatus(next);
        return toResponse(orderRepository.save(order));
    }

    // ─── helpers ───────────────────────────────────────────────────────────────

    private void validateTransition(OrderStatus from, OrderStatus to) {
        if (TERMINAL.contains(from)) {
            throw new InvalidStatusTransitionException(from.name(), to.name());
        }
        boolean valid = switch (from) {
            case PENDING    -> to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING -> to == OrderStatus.COMPLETED  || to == OrderStatus.CANCELLED;
            case COMPLETED  -> to == OrderStatus.CANCELLED; // hoàn đơn — cộng lại stock
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

    private Long getCurrentUserId() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("userId");
    }

    private String getCurrentScope() {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return jwt.getClaim("scope");
    }

    static OrderResponse toResponse(Order order) {
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
}
