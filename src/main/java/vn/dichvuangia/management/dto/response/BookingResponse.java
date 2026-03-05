package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.dichvuangia.management.common.enums.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Builder
public class BookingResponse {

    private Long id;
    private String bookingCode;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private String notes;
    private LocalDateTime createdAt;

    // Thông tin khách hàng
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerAddress;

    // Dịch vụ
    private Long serviceId;
    private String serviceName;
    private java.math.BigDecimal serviceBasePrice;

    // Kỹ thuật viên (nullable — chỉ có sau khi ADMIN gán)
    private Long technicianId;
    private String technicianUsername;
}
