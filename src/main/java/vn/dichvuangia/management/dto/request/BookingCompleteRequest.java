package vn.dichvuangia.management.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingCompleteRequest {

    // Technician điền ghi chú công việc đã làm khi hoàn thành (không bắt buộc)
    private String notes;
}
