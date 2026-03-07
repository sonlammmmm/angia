package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.ProductType;

import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class ProductCreateRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String productCode;

    @NotNull(message = "Loại sản phẩm không được để trống")
    private ProductType productType;

    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    private String model;

    private String description;

    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity = 0;

    // Dùng cho MACHINE
    private Integer warrantyMonths;

    // Dùng cho FILTER
    private Integer lifespanMonths;

    // Thông số kỹ thuật dạng key-value: {"flow_rate":"75GPD","origin":"Korea"}
    // Sẽ được serialize sang JSON string khi lưu vào DB
    // Tên field khớp với API spec: specsJson
    private Map<String, Object> specsJson;
}
