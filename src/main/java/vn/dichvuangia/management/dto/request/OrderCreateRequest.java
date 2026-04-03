package vn.dichvuangia.management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * DTO tạo đơn hàng (Staff / Sale).
 * <p>
 * Flow: Nhập SĐT → nếu đã có Customer thì tự fill thông tin,
 *       nếu chưa có thì hệ thống tạo mới Customer với fullName + phone.
 * <p>
 * - customerId (optional): nếu có thì dùng Customer sẵn.
 * - phone + fullName: bắt buộc khi không có customerId — dùng để tìm/tạo Customer.
 */
@Getter
@Setter
public class OrderCreateRequest {

    // Nếu đã chọn khách hàng cụ thể (optional)
    private Long customerId;

    // Thông tin khách hàng — bắt buộc khi không có customerId
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String fullName;

    private String email;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;

    // Ghi chú của khách (không bắt buộc)
    private String notes;
}
