package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BrandResponse {

    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private LocalDateTime createdAt;
}
