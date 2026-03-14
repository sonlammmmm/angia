package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceUpdateRequest {

    private String name;
    private String description;

    @DecimalMin(value = "0.0", inclusive = true, message = "Giá không được âm")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Thời gian thực hiện phải ít nhất 1 phút")
    private Integer durationMinutes;
}
