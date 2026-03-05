package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByIdAndIsActiveTrue(Long id);

    boolean existsByUsername(String username);
}
