package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Lấy tin nhắn theo conversation, phân trang (mới nhất trước) */
    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    /** Lấy tất cả tin nhắn của conversation (cho load history) */
    List<ChatMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /** Đánh dấu tất cả tin nhắn chưa đọc là đã đọc (cho một phía) */
    @Modifying
    @Query("""
        UPDATE ChatMessage m SET m.seen = true
        WHERE m.conversation.id = :conversationId
          AND m.sender.id <> :readerId
          AND m.seen = false
    """)
    int markAsSeenByConversationAndReader(
        @Param("conversationId") Long conversationId,
        @Param("readerId") Long readerId
    );
}
