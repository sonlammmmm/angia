package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // SALE scope: chỉ xem khách do mình tạo (created_by = currentUserId)
    Page<Customer> findAllByCreatedBy_Id(Long userId, Pageable pageable);

    // Search by fullName or phone (case-insensitive)
    @Query("""
            SELECT c FROM Customer c
            WHERE (:q IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR c.phone LIKE CONCAT('%', :q, '%'))
            """)
    Page<Customer> searchAll(@Param("q") String q, Pageable pageable);

    // Search by fullName or phone — SALE scope
    @Query("""
            SELECT c FROM Customer c
            WHERE c.createdBy.id = :userId
              AND (:q IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                                OR c.phone LIKE CONCAT('%', :q, '%'))
            """)
    Page<Customer> searchAllByCreatedBy(@Param("userId") Long userId, @Param("q") String q, Pageable pageable);

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    Optional<Customer> findByEmail(String email);

    // Tìm Customer profile theo User ID (cho khách hàng tự đăng ký — createdBy = chính mình)
    Optional<Customer> findByCreatedBy_Id(Long userId);
}

