package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.dichvuangia.management.common.enums.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ProductResponse {

    private Long id;
    private String productCode;
    private ProductType productType;
    private Long brandId;
    private String brandName;
    private String name;
    private String model;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer warrantyMonths;
    private Integer lifespanMonths;

    // specs_json deserialize sang Map để frontend dùng dễ hơn
    // Tên field khớp với API spec: specsJson
    private Map<String, Object> specsJson;

    private List<ProductImageResponse> images;
    private LocalDateTime createdAt;
}
