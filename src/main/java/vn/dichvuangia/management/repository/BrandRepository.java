package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.Brand;

import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {

    Page<Brand> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Brand> findByIdAndIsDeletedFalse(Long id);
}
