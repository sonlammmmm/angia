package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin 1 tin nhắn.
 */
@Getter
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;         // username đăng nhập
    private String senderDisplayName;  // tên hiển thị (fullName nếu có, fallback username)
    private String senderRole;
    private String content;
    private String type;
    private Boolean seen;
    private LocalDateTime createdAt;
}
