package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductImageResponse {

    private Long id;
    private String imageUrl;
    private Boolean isMain;
}
