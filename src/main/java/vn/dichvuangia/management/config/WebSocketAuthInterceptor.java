package vn.dichvuangia.management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Interceptor xác thực JWT cho kết nối WebSocket STOMP.
 * Client gửi token qua STOMP header "Authorization" khi CONNECT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor
                .getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    String username = jwt.getSubject();
                    Long userId = jwt.getClaim("userId");
                    String scope = jwt.getClaim("scope"); // "ROLE_ADMIN", "ROLE_CUSTOMER"...

                    var authorities = List.of(new SimpleGrantedAuthority(scope));
                    var authentication = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    // Lưu userId vào details để dùng sau
                    authentication.setDetails(userId);

                    accessor.setUser(authentication);
                    log.debug("WebSocket CONNECT authenticated: user={}, userId={}, role={}",
                            username, userId, scope);
                } catch (Exception e) {
                    log.warn("WebSocket CONNECT JWT invalid: {}", e.getMessage());
                    throw new IllegalArgumentException("Invalid JWT token");
                }
            } else {
                log.warn("WebSocket CONNECT without Authorization header");
                throw new IllegalArgumentException("Missing Authorization header");
            }
        }

        return message;
    }
}
