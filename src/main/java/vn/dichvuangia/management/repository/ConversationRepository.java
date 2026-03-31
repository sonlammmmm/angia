package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.enums.ConversationStatus;
import vn.dichvuangia.management.entity.Conversation;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /** Tìm conversation WAITING hoặc ACTIVE của 1 customer */
    Optional<Conversation> findByCustomerIdAndStatusIn(Long customerId, List<ConversationStatus> statuses);

    /** Danh sách conversation theo status, sắp xếp mới nhất */
    List<Conversation> findByStatusOrderByLastMessageAtDesc(ConversationStatus status);

    /** Tất cả conversation chưa đóng, sắp xếp mới nhất */
    List<Conversation> findByStatusInOrderByLastMessageAtDesc(List<ConversationStatus> statuses);

    /** Tất cả conversation được gán cho 1 admin */
    List<Conversation> findByAssignedAdminIdAndStatusOrderByLastMessageAtDesc(Long adminId, ConversationStatus status);

    /** Đếm số conversation đang active của 1 admin (để tính load) */
    long countByAssignedAdminIdAndStatus(Long adminId, ConversationStatus status);

    /** Tìm admin có ít conversation active nhất trong danh sách admin online */
    @Query("""
        SELECT c.assignedAdmin.id, COUNT(c) as cnt
        FROM Conversation c
        WHERE c.assignedAdmin.id IN :adminIds AND c.status = 'ACTIVE'
        GROUP BY c.assignedAdmin.id
        ORDER BY cnt ASC
    """)
    List<Object[]> countActiveByAdminIds(@Param("adminIds") List<Long> adminIds);
}
