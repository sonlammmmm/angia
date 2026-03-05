package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.common.enums.OrderStatus;
import vn.dichvuangia.management.entity.Order;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllBySaleId(Long saleId, Pageable pageable);

    Page<Order> findAllByCustomerId(Long customerId, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
