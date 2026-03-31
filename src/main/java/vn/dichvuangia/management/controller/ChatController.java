package vn.dichvuangia.management.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.dichvuangia.management.common.ApiResponse;
import vn.dichvuangia.management.dto.response.ChatMessageResponse;
import vn.dichvuangia.management.dto.response.ConversationResponse;
import vn.dichvuangia.management.service.ChatService;

import java.util.List;
import java.util.Set;

/**
 * REST Controller cho các thao tác chat (lấy danh sách, history, takeover...).
 * Các thao tác realtime (gửi tin nhắn, typing, seen) nằm ở ChatWebSocketController.
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // ── Customer endpoints ────────────────────────────────────────────────

    /**
     * Customer lấy conversation hiện tại (nếu có).
     */
    @GetMapping("/my-conversation")
    public ResponseEntity<ApiResponse<ConversationResponse>> getMyConversation(
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        ConversationResponse conversation = chatService.getCustomerConversation(userId);
        return ResponseEntity.ok(ApiResponse.success(conversation));
    }

    // ── Admin endpoints ───────────────────────────────────────────────────

    /**
     * Admin lấy danh sách tất cả conversation (WAITING + ACTIVE).
     */
    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getAllConversations() {
        return ResponseEntity.ok(ApiResponse.success(chatService.getAllConversations()));
    }

    /**
     * Lấy chat history của 1 conversation.
     */
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getMessages(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(chatService.getMessages(id)));
    }

    /**
     * Admin takeover conversation.
     */
    @PostMapping("/conversations/{id}/takeover")
    public ResponseEntity<ApiResponse<ConversationResponse>> takeoverConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long adminId = jwt.getClaim("userId");
        ConversationResponse response = chatService.takeoverConversation(adminId, id);
        return ResponseEntity.ok(ApiResponse.success("Đã nhận conversation", response));
    }

    /**
     * Đóng conversation.
     */
    @PostMapping("/conversations/{id}/close")
    public ResponseEntity<ApiResponse<ConversationResponse>> closeConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        Long userId = jwt.getClaim("userId");
        ConversationResponse response = chatService.closeConversation(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Đã đóng conversation", response));
    }

    /**
     * Lấy danh sách admin online.
     */
    @GetMapping("/online-admins")
    public ResponseEntity<ApiResponse<Set<Long>>> getOnlineAdmins() {
        return ResponseEntity.ok(ApiResponse.success(chatService.getOnlineAdminIds()));
    }
}
