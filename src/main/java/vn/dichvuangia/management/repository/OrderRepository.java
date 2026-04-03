package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.entity.Order;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllBySaleId(Long saleId, Pageable pageable);

    Page<Order> findAllByCustomerId(Long customerId, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    // Filter nâng cao: saleId + status (dùng cho SALE xem đơn của mình theo trạng thái)
    Page<Order> findAllBySaleIdAndStatus(Long saleId, OrderStatus status, Pageable pageable);

    // Filter nâng cao tổng hợp (ADMIN/MANAGEMENT xem toàn bộ)
    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:saleId IS NULL OR o.sale.id = :saleId)
              AND (:customerId IS NULL OR o.customer.id = :customerId)
              AND (:from IS NULL OR o.createdAt >= :from)
              AND (:to IS NULL OR o.createdAt <= :to)
            """)
    Page<Order> findAllWithFilter(
            @Param("status") OrderStatus status,
            @Param("saleId") Long saleId,
            @Param("customerId") Long customerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}
