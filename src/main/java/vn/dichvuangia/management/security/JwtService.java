package vn.dichvuangia.management.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import vn.dichvuangia.management.entity.User;

import java.time.Instant;

/**
 * Tạo Access Token theo chuẩn JWT HMAC-SHA256 dùng NimbusJwtEncoder.
 * Không dùng jjwt hay bất kỳ JWT lib nào khác (theo 04_SECURITY.md §1).
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${app.jwt.expiration:3600}")
    private long expiration;

    /**
     * Sinh Access Token với claims: userId, scope (ROLE_*).
     * Spring Security tự đọc claim "scope" → set GrantedAuthority → hasRole() hoạt động.
     */
    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("vn.dichvuangia")
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiration))
                .claim("userId", user.getId())
                .claim("scope", "ROLE_" + user.getRole().getName())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
