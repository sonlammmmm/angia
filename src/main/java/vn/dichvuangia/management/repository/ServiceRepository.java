package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.Service;

import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    Page<Service> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Service> findByIdAndIsDeletedFalse(Long id);
}
