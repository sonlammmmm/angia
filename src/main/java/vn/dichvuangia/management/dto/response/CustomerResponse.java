package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CustomerResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String address;
    private LocalDateTime createdAt;

    // Nhân viên tạo hồ sơ (Sale)
    private Long createdById;
    private String createdByUsername;
}
