package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.common.OrderStatus;
import vn.dichvuangia.management.entity.Order;
import vn.dichvuangia.management.entity.User;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // SALE chỉ thấy đơn của mình
    Page<Order> findAllBySale(User sale, Pageable pageable);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByOrderCode(String orderCode);

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);
}
