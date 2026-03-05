package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandCreateRequest {

    @NotBlank(message = "Tên thương hiệu không được để trống")
    private String name;

    private String description;

    private String logoUrl;
}
