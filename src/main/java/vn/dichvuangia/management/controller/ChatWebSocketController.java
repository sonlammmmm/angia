package vn.dichvuangia.management.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import vn.dichvuangia.management.dto.request.ChatMessageRequest;
import vn.dichvuangia.management.dto.response.ChatMessageResponse;
import vn.dichvuangia.management.service.ChatService;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket STOMP Controller xử lý các events chat realtime:
 * - Gửi tin nhắn
 * - Typing indicator
 * - Seen status
 * - Admin online/offline
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Gửi tin nhắn từ Customer ──────────────────────────────────────────

    /**
     * Client gửi tới: /app/chat.customer.send
     * Broadcast tới: /topic/chat/{conversationId}
     */
    @MessageMapping("chat.customer.send")
    public void customerSendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long userId = extractUserId(principal);
        log.debug("Customer {} sending message", userId);
        ChatMessageResponse response = chatService.customerSendMessage(userId, request);
        log.debug("Message sent: id={}, conversationId={}", response.getId(), response.getConversationId());
    }

    // ── Gửi tin nhắn từ Admin ─────────────────────────────────────────────

    /**
     * Client gửi tới: /app/chat.admin.send
     * Broadcast tới: /topic/chat/{conversationId}
     */
    @MessageMapping("chat.admin.send")
    public void adminSendMessage(@Payload ChatMessageRequest request, Principal principal) {
        Long userId = extractUserId(principal);
        log.debug("Admin {} sending message to conversation {}", userId, request.getConversationId());
        ChatMessageResponse response = chatService.adminSendMessage(userId, request);
        log.debug("Message sent: id={}", response.getId());
    }

    // ── Typing Indicator ──────────────────────────────────────────────────

    /**
     * Client gửi tới: /app/chat.typing.{conversationId}
     * Broadcast tới: /topic/chat/{conversationId}/typing
     */
    @MessageMapping("chat.typing.{conversationId}")
    public void typing(@DestinationVariable Long conversationId, Principal principal) {
        Long userId = extractUserId(principal);
        String username = principal.getName();

        Map<String, Object> typingEvent = new HashMap<>();
        typingEvent.put("conversationId", conversationId);
        typingEvent.put("userId", userId);
        typingEvent.put("username", username);
        typingEvent.put("timestamp", System.currentTimeMillis());

        messagingTemplate.convertAndSend(
                "/topic/chat/" + conversationId + "/typing", typingEvent);
    }

    // ── Seen Status ───────────────────────────────────────────────────────

    /**
     * Client gửi tới: /app/chat.seen.{conversationId}
     * Broadcast tới: /topic/chat/{conversationId}/seen
     */
    @MessageMapping("chat.seen.{conversationId}")
    public void markSeen(@DestinationVariable Long conversationId, Principal principal) {
        Long userId = extractUserId(principal);
        chatService.markAsSeen(conversationId, userId);
    }

    // ── Admin Online/Offline ──────────────────────────────────────────────

    /**
     * Admin gửi khi connect: /app/chat.admin.online
     */
    @MessageMapping("chat.admin.online")
    public void adminOnline(Principal principal) {
        Long userId = extractUserId(principal);
        chatService.adminOnline(userId);

        // Broadcast admin online status
        Map<String, Object> event = new HashMap<>();
        event.put("adminId", userId);
        event.put("username", principal.getName());
        event.put("online", true);
        messagingTemplate.convertAndSend("/topic/admin/status", event);
    }

    /**
     * Admin gửi khi disconnect: /app/chat.admin.offline
     */
    @MessageMapping("chat.admin.offline")
    public void adminOffline(Principal principal) {
        Long userId = extractUserId(principal);
        chatService.adminOffline(userId);

        Map<String, Object> event = new HashMap<>();
        event.put("adminId", userId);
        event.put("username", principal.getName());
        event.put("online", false);
        messagingTemplate.convertAndSend("/topic/admin/status", event);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Long extractUserId(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            return (Long) auth.getDetails();
        }
        throw new RuntimeException("Cannot extract userId from principal");
    }
}
