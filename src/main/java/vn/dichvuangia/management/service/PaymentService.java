package vn.dichvuangia.management.service;

import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.dichvuangia.management.common.enums.BookingStatus;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.common.enums.PaymentMethod;
import vn.dichvuangia.management.common.enums.PaymentReferenceType;
import vn.dichvuangia.management.common.enums.PaymentStatus;
import vn.dichvuangia.management.dto.request.PaypalCreateRequest;
import vn.dichvuangia.management.dto.response.PaypalCreateResponse;
import vn.dichvuangia.management.dto.response.PaypalExecuteResponse;
import vn.dichvuangia.management.entity.MaintenanceBooking;
import vn.dichvuangia.management.entity.Order;
import vn.dichvuangia.management.entity.Payment;
import vn.dichvuangia.management.exception.ResourceNotFoundException;
import vn.dichvuangia.management.repository.MaintenanceBookingRepository;
import vn.dichvuangia.management.repository.OrderRepository;
import vn.dichvuangia.management.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MaintenanceBookingRepository bookingRepository;
    private final PaypalService paypalService;

    @Value("${paypal.exchange-rate:25000}")
    private double exchangeRate;

    @Value("${paypal.currency:USD}")
    private String paypalCurrency;

    @Value("${paypal.frontend-success-url}")
    private String successUrl;

    @Value("${paypal.frontend-cancel-url}")
    private String cancelUrl;

    @Transactional
    public PaypalCreateResponse createPaypalPayment(PaypalCreateRequest request) throws PayPalRESTException {
        if (request.getReferenceType() == PaymentReferenceType.ORDER) {
            return createOrderPayment(request.getReferenceId());
        }
        return createBookingPayment(request.getReferenceId());
    }

    @Transactional
    public PaypalExecuteResponse executePaypalPayment(String paymentId, String payerId) throws PayPalRESTException {
    Payment paymentRecord = paymentRepository.findByPaypalPaymentId(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy giao dịch PayPal: " + paymentId));

    com.paypal.api.payments.Payment executed = paypalService.executePayment(paymentId, payerId);

        if ("approved".equalsIgnoreCase(executed.getState())) {
            paymentRecord.setStatus(PaymentStatus.APPROVED);
            paymentRecord.setPaypalPayerId(payerId);
            paymentRepository.save(paymentRecord);

            if (paymentRecord.getReferenceType() == PaymentReferenceType.ORDER) {
                Long orderId = java.util.Objects.requireNonNull(paymentRecord.getReferenceId(), "Order id is required");
                Order order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
                if (order.getStatus() == OrderStatus.PENDING) {
                    order.setStatus(OrderStatus.PROCESSING);
                    orderRepository.save(order);
                }
            }

            return PaypalExecuteResponse.builder()
                    .status(PaymentStatus.APPROVED)
                    .referenceType(paymentRecord.getReferenceType())
                    .referenceId(paymentRecord.getReferenceId())
                    .referenceCode(paymentRecord.getReferenceCode())
                    .message("Thanh toán thành công")
                    .build();
        }

        paymentRecord.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(paymentRecord);

        return PaypalExecuteResponse.builder()
                .status(PaymentStatus.FAILED)
                .referenceType(paymentRecord.getReferenceType())
                .referenceId(paymentRecord.getReferenceId())
                .referenceCode(paymentRecord.getReferenceCode())
                .message("Thanh toán chưa được duyệt")
                .build();
    }

    private PaypalCreateResponse createOrderPayment(Long orderId) throws PayPalRESTException {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id không hợp lệ");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        BigDecimal amountVnd = order.getTotalAmount();
        BigDecimal amountUsd = convertToUsd(amountVnd);

    com.paypal.api.payments.Payment paypalPayment = paypalService.createPayment(
                amountUsd.doubleValue(),
                paypalCurrency,
                "Thanh toan don hang An Gia",
                cancelUrl,
                successUrl
        );

        String approvalUrl = paypalService.getApprovalUrl(paypalPayment);

    Payment record = new Payment();
        record.setReferenceType(PaymentReferenceType.ORDER);
        record.setReferenceId(order.getId());
        record.setReferenceCode(order.getOrderCode());
        record.setAmountVnd(amountVnd);
        record.setAmountUsd(amountUsd);
        record.setCurrency(paypalCurrency);
        record.setMethod(PaymentMethod.PAYPAL);
        record.setStatus(PaymentStatus.CREATED);
        record.setPaypalPaymentId(paypalPayment.getId());
        record.setApprovalUrl(approvalUrl);
        paymentRepository.save(record);

        return PaypalCreateResponse.builder()
                .paymentId(record.getId())
                .paypalPaymentId(record.getPaypalPaymentId())
                .status(record.getStatus())
                .approvalUrl(record.getApprovalUrl())
                .referenceType(record.getReferenceType())
                .referenceId(record.getReferenceId())
                .referenceCode(record.getReferenceCode())
                .amountVnd(record.getAmountVnd())
                .amountUsd(record.getAmountUsd())
                .message("Tạo thanh toán thành công")
                .build();
    }

    private PaypalCreateResponse createBookingPayment(Long bookingId) throws PayPalRESTException {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking id không hợp lệ");
        }
        MaintenanceBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceBooking", bookingId));

        BigDecimal amountVnd = booking.getService().getBasePrice();
        if (amountVnd == null || amountVnd.compareTo(BigDecimal.ZERO) <= 0) {
            Payment record = new Payment();
            record.setReferenceType(PaymentReferenceType.BOOKING);
            record.setReferenceId(booking.getId());
            record.setReferenceCode(booking.getBookingCode());
            record.setAmountVnd(BigDecimal.ZERO);
            record.setAmountUsd(BigDecimal.ZERO);
            record.setCurrency(paypalCurrency);
            record.setMethod(PaymentMethod.PAYPAL);
            record.setStatus(PaymentStatus.FREE);
            paymentRepository.save(record);

            if (booking.getStatus() == BookingStatus.PENDING) {
                bookingRepository.save(booking);
            }

            return PaypalCreateResponse.builder()
                    .paymentId(record.getId())
                    .status(record.getStatus())
                    .referenceType(record.getReferenceType())
                    .referenceId(record.getReferenceId())
                    .referenceCode(record.getReferenceCode())
                    .amountVnd(record.getAmountVnd())
                    .amountUsd(record.getAmountUsd())
                    .message("Lịch hẹn miễn phí - không cần thanh toán")
                    .build();
        }

        BigDecimal amountUsd = convertToUsd(amountVnd);
    com.paypal.api.payments.Payment paypalPayment = paypalService.createPayment(
                amountUsd.doubleValue(),
                paypalCurrency,
                "Thanh toan lich bao tri An Gia",
                cancelUrl,
                successUrl
        );

        String approvalUrl = paypalService.getApprovalUrl(paypalPayment);

    Payment record = new Payment();
        record.setReferenceType(PaymentReferenceType.BOOKING);
        record.setReferenceId(booking.getId());
        record.setReferenceCode(booking.getBookingCode());
        record.setAmountVnd(amountVnd);
        record.setAmountUsd(amountUsd);
        record.setCurrency(paypalCurrency);
        record.setMethod(PaymentMethod.PAYPAL);
        record.setStatus(PaymentStatus.CREATED);
        record.setPaypalPaymentId(paypalPayment.getId());
        record.setApprovalUrl(approvalUrl);
        paymentRepository.save(record);

        return PaypalCreateResponse.builder()
                .paymentId(record.getId())
                .paypalPaymentId(record.getPaypalPaymentId())
                .status(record.getStatus())
                .approvalUrl(record.getApprovalUrl())
                .referenceType(record.getReferenceType())
                .referenceId(record.getReferenceId())
                .referenceCode(record.getReferenceCode())
                .amountVnd(record.getAmountVnd())
                .amountUsd(record.getAmountUsd())
                .message("Tạo thanh toán thành công")
                .build();
    }

    private BigDecimal convertToUsd(BigDecimal amountVnd) {
        if (amountVnd == null) {
            return BigDecimal.ZERO;
        }
        return amountVnd
                .divide(BigDecimal.valueOf(exchangeRate), 2, RoundingMode.HALF_UP);
    }
}
