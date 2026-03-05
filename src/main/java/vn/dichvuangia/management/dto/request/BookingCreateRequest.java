package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingCreateRequest {

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @NotNull(message = "Dịch vụ không được để trống")
    private Long serviceId;

    @NotNull(message = "Ngày đặt lịch không được để trống")
    @Future(message = "Ngày đặt lịch phải là ngày trong tương lai")
    private LocalDateTime bookingDate;

    // Ghi chú của khách (không bắt buộc)
    private String notes;
}
