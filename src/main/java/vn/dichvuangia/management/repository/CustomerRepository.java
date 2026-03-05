package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findAllByCreatedById(Long userId, Pageable pageable);

    Optional<Customer> findByPhone(String phone);

    boolean existsByPhone(String phone);
}
