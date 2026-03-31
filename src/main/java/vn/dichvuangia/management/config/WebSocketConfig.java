package vn.dichvuangia.management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Cấu hình WebSocket STOMP cho hệ thống chat realtime.
 *
 * - Endpoint: /ws (SockJS fallback)
 * - App prefix: /app (client gửi message tới /app/*)
 * - Broker: /topic (broadcast), /queue (point-to-point)
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor) {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Simple in-memory broker cho /topic và /queue
        config.enableSimpleBroker("/topic", "/queue");
        // Prefix cho @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // Prefix cho user-specific messages (convertAndSendToUser)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        // Raw WebSocket (dùng cho @stomp/stompjs native)
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins.toArray(new String[0]));
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Xác thực JWT khi STOMP CONNECT
        registration.interceptors(webSocketAuthInterceptor);
    }
}
