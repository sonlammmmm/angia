package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.ProductSpec;

import java.util.List;

public interface ProductSpecRepository extends JpaRepository<ProductSpec, Long> {

    List<ProductSpec> findAllByOrderByDisplayOrderAsc();
}
