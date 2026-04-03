package vn.dichvuangia.management.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.dichvuangia.management.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /** Eager fetch role để tránh LazyInitializationException trong UserDetailsServiceImpl */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.username = :username")
    Optional<User> findByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.id = :id AND u.isActive = true")
    Optional<User> findByIdAndIsActiveTrue(@Param("id") Long id);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    /** Lấy danh sách nhân viên (loại trừ CUSTOMER) */
    @Query("SELECT u FROM User u JOIN u.role r WHERE r.name <> 'CUSTOMER'")
    Page<User> findAllStaff(Pageable pageable);
}
