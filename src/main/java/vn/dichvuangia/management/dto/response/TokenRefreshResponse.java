package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenRefreshResponse {

    private String accessToken;

    private String tokenType;
}
