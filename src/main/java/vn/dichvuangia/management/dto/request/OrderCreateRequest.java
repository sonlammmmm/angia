package vn.dichvuangia.management.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequest {

    @NotNull(message = "Khách hàng không được để trống")
    private Long customerId;

    @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
    @Valid
    private List<OrderItemRequest> items;

    @NotBlank(message = "Địa chỉ giao hàng không được để trống")
    private String shippingAddress;
}
