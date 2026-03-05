package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import vn.dichvuangia.management.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {

    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    boolean existsByProductCode(String productCode);
}
