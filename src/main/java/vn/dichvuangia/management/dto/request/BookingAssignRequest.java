package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingAssignRequest {

    @NotNull(message = "Kỹ thuật viên không được để trống")
    private Long technicianId;
}
