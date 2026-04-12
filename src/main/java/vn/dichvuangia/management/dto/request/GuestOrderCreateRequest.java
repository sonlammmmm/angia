package vn.dichvuangia.management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.PaymentMethod;

import java.util.List;

/**
 * DTO cho khách vãng lai đặt hàng — không cần đăng nhập.
 * Hệ thống tự tìm/tạo Customer theo số điện thoại.
 */
@Getter
@Setter
public class GuestOrderCreateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    // Phương thức thanh toán (PAYPAL | CASH). Nếu null sẽ giữ behavior cũ.
    private PaymentMethod paymentMethod;

    // Ghi chú của khách (không bắt buộc)
    private String notes;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> items;
}
