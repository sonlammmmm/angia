package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String fullName;   // fullName từ bảng Customer (chỉ có với role CUSTOMER)
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
