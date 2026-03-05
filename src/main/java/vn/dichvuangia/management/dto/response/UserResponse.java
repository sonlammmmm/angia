package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {

    private Long id;
    private String username;
    private String roleName;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
