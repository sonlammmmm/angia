package vn.dichvuangia.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO nhận tin nhắn từ client (qua WebSocket hoặc REST).
 */
@Getter
@Setter
public class ChatMessageRequest {

    /** ID conversation (null nếu customer gửi lần đầu → tạo mới) */
    private Long conversationId;

    @NotBlank(message = "Nội dung tin nhắn không được trống")
    @Size(max = 2000, message = "Tin nhắn tối đa 2000 ký tự")
    private String content;
}
