package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ProductUpdateRequest {

    private Long brandId;
    private String name;
    private String model;
    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho không được âm")
    private Integer stockQuantity;

    private Integer warrantyMonths;
    private Integer lifespanMonths;

    // null = giữ nguyên specs hiện tại
    private Map<String, Object> specsJson;

    // null = giữ nguyên ảnh hiện tại
    // Danh sách URL ảnh mới (phần tử đầu = isMain = true)
    private List<String> imageUrls;
}
