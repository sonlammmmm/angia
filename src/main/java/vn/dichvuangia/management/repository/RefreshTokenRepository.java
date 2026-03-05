package vn.dichvuangia.management.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.dichvuangia.management.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByUserId(Long userId);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
