package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ServiceResponse {

    private Long id;
    private String serviceCode;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
}
