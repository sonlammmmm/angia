package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(String name);
}
