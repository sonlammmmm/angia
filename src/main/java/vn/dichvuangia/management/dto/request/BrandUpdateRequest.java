package vn.dichvuangia.management.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BrandUpdateRequest {

    // null = giữ nguyên giá trị cũ
    private String name;
    private String description;
    private String logoUrl;
}
