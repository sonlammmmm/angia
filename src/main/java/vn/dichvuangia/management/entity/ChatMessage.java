package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.MessageType;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity đại diện cho một tin nhắn trong cuộc hội thoại.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chat_messages_conversation", columnList = "conversation_id"),
    @Index(name = "idx_chat_messages_created_at", columnList = "created_at")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cuộc hội thoại chứa tin nhắn này */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /** Người gửi (có thể là customer hoặc admin, null nếu SYSTEM message) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    /** Nội dung tin nhắn */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** Loại tin nhắn */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType type = MessageType.TEXT;

    /** Tin nhắn đã được đọc chưa */
    @Column(name = "seen", nullable = false)
    private Boolean seen = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;
        ChatMessage other = (ChatMessage) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
