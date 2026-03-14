package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ServiceCreateRequest {

    private String serviceCode;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    private String name;

    private String description;

    @NotNull(message = "Giá dịch vụ không được để trống")
    @DecimalMin(value = "0.0", inclusive = true, message = "Giá không được âm")
    private BigDecimal basePrice;

    @Min(value = 1, message = "Thời gian thực hiện phải ít nhất 1 phút")
    private Integer durationMinutes;
}
