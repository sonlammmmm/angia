package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.OrderStatus;

@Getter
@Setter
public class OrderStatusUpdateRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;
}
