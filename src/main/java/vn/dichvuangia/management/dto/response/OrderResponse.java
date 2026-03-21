package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;
import vn.dichvuangia.management.common.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class OrderResponse {

    private Long id;
    private String orderCode;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Thông tin khách hàng (embed nhẹ — không lồng toàn bộ CustomerResponse)
    private Long customerId;
    private String customerName;
    private String customerPhone;

    // Sale phụ trách
    private Long saleId;
    private String saleUsername;

    private List<OrderItemResponse> items;
}
