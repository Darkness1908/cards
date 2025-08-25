package com.effective.cards.repository;

import com.effective.cards.model.RefreshJwt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshJwtRepository extends JpaRepository<RefreshJwt, Long> {
    boolean existsByToken(String token);
    Optional<RefreshJwt> findByToken(String refreshToken);
}
