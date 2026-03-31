package vn.dichvuangia.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.dichvuangia.management.common.enums.ConversationStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity đại diện cho một cuộc hội thoại (ticket) giữa khách hàng và admin.
 * Mỗi khách hàng tại một thời điểm chỉ có tối đa 1 conversation ACTIVE/WAITING.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Khách hàng (User có role CUSTOMER) tạo conversation */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    /** Admin/Management/Sale được gán xử lý (có thể null nếu đang waiting) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConversationStatus status = ConversationStatus.WAITING;

    /** Nội dung tin nhắn cuối cùng (để hiển thị preview) */
    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    /** Thời điểm tin nhắn cuối */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /** Số tin nhắn chưa đọc bởi admin */
    @Column(name = "unread_count_admin", nullable = false)
    private Integer unreadCountAdmin = 0;

    /** Số tin nhắn chưa đọc bởi customer */
    @Column(name = "unread_count_customer", nullable = false)
    private Integer unreadCountCustomer = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastMessageAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conversation)) return false;
        Conversation other = (Conversation) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
