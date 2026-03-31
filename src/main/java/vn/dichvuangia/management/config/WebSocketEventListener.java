package vn.dichvuangia.management.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import vn.dichvuangia.management.service.ChatService;

/**
 * Listener cho WebSocket session events.
 * Xử lý admin offline khi disconnect (mất kết nối, đóng tab...).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final ChatService chatService;

    @EventListener
    public void handleWebSocketConnect(SessionConnectEvent event) {
        var principal = event.getUser();
        if (principal != null) {
            log.debug("WebSocket connected: user={}", principal.getName());
        }
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        var principal = event.getUser();
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            Long userId = (Long) auth.getDetails();
            String scope = auth.getAuthorities().iterator().next().getAuthority();

            // Chỉ xử lý admin disconnect
            if (!scope.equals("ROLE_CUSTOMER")) {
                chatService.adminOffline(userId);
                log.info("Admin disconnected (session closed): userId={}", userId);
            }
        }
    }
}
