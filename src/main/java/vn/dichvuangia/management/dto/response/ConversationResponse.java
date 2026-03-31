package vn.dichvuangia.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO trả về thông tin conversation cho danh sách sidebar.
 */
@Getter
@Builder
public class ConversationResponse {
    private Long id;
    private Long customerId;
    private String customerName;      // fullName từ bảng Customer (hoặc username nếu chưa có)
    private String customerUsername;   // username đăng nhập
    private Long assignedAdminId;
    private String assignedAdminName;
    private String status;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private Integer unreadCountAdmin;
    private Integer unreadCountCustomer;
    private LocalDateTime createdAt;
}
