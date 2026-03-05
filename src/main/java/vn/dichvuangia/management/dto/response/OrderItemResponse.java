package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productCode;
    private Integer quantity;
    private BigDecimal unitPrice;

    // Thành tiền = quantity * unitPrice (tính sẵn cho frontend)
    private BigDecimal subtotal;
}
