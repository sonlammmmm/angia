package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO cho khách vãng lai đặt lịch bảo trì — không cần đăng nhập.
 * Hệ thống tự tìm/tạo Customer theo số điện thoại.
 */
@Getter
@Setter
public class GuestBookingCreateRequest {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    private String address;

    @NotNull(message = "Dịch vụ không được để trống")
    private Long serviceId;

    @NotNull(message = "Ngày đặt lịch không được để trống")
    @Future(message = "Ngày đặt lịch phải là ngày trong tương lai")
    private LocalDateTime bookingDate;

    private String notes;
}
