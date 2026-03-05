package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByIsDeletedFalseAndProductType(ProductType productType, Pageable pageable);

    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    boolean existsByProductCode(String productCode);
}
