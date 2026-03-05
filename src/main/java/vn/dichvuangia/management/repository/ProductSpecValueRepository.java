package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.ProductSpecValue;

import java.util.List;

public interface ProductSpecValueRepository extends JpaRepository<ProductSpecValue, Long> {

    List<ProductSpecValue> findAllByProductId(Long productId);

    void deleteAllByProductId(Long productId);
}
