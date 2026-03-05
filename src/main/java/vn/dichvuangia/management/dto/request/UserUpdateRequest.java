package vn.dichvuangia.management.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    // Không bắt buộc — chỉ update field nào được gửi lên (null = giữ nguyên)
    private String password;

    private Long roleId;

    private Boolean isActive;
}
