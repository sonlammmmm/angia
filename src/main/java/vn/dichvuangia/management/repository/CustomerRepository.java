package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // SALE scope: chỉ xem khách do mình tạo (created_by = currentUserId)
    Page<Customer> findAllByCreatedBy_Id(Long userId, Pageable pageable);

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    // Tìm Customer profile theo User ID (cho khách hàng tự đăng ký — createdBy = chính mình)
    Optional<Customer> findByCreatedBy_Id(Long userId);
}

