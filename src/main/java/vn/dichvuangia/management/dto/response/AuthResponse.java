package vn.dichvuangia.management.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;

    // seconds — client dùng để biết token hết hạn khi nào
    private long expiresIn;

    // @JsonIgnore — không trả ra body JSON, chỉ dùng nội bộ để Controller set Cookie
    @JsonIgnore
    private String refreshToken;
}
