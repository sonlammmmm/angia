package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO tạo lịch bảo trì (Staff / Sale).
 * <p>
 * Flow: Chọn khách hàng có sẵn (customerId) hoặc nhập SĐT + Họ tên
 *       để tìm/tạo khách hàng mới — hỗ trợ khách mua trực tiếp.
 * <p>
 * - customerId (optional): nếu có thì dùng Customer sẵn.
 * - phone + fullName: bắt buộc khi không có customerId — dùng để tìm/tạo Customer.
 */
@Getter
@Setter
public class BookingCreateRequest {

    // Nếu đã chọn khách hàng cụ thể (optional)
    private Long customerId;

    // Thông tin khách hàng — bắt buộc khi không có customerId
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String fullName;

    private String address;

    @NotNull(message = "Dịch vụ không được để trống")
    private Long serviceId;

    @NotNull(message = "Ngày đặt lịch không được để trống")
    @Future(message = "Ngày đặt lịch phải là ngày trong tương lai")
    private LocalDateTime bookingDate;

    // Ghi chú của khách (không bắt buộc)
    private String notes;
}
