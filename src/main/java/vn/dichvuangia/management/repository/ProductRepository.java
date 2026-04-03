package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.common.enums.ProductType;
import vn.dichvuangia.management.entity.Product;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAllByIsDeletedFalseAndProductType(ProductType productType, Pageable pageable);

    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Product> findByIdAndIsDeletedFalse(Long id);

    boolean existsByProductCode(String productCode);

    // Filter theo type + brand + search keyword (nullable — nếu null thì bỏ qua điều kiện đó)
    @Query("""
            SELECT p FROM Product p
            WHERE p.isDeleted = false
              AND (:type IS NULL OR p.productType = :type)
              AND (:brandId IS NULL OR p.brand.id = :brandId)
              AND (:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%'))
                               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<Product> findAllWithFilter(
            @Param("type") ProductType type,
            @Param("brandId") Long brandId,
            @Param("q") String q,
            Pageable pageable);
}
