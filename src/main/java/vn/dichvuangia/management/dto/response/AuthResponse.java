package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {

    private String accessToken;

    // refreshToken được set vào HttpOnly Cookie, không trả trong body
    private String tokenType;

    private UserResponse user;
}

