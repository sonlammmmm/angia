package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.RefreshToken;
import vn.dichvuangia.management.entity.User;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // Xóa tất cả refresh token của 1 user khi logout
    void deleteAllByUser(User user);
}
