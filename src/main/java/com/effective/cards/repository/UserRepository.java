package com.effective.cards.repository;

import com.effective.cards.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phoneNumber);

    @Query("SELECT u.phone FROM User u WHERE u.id = :userId")
    String findPhoneById(@Param("userId") Long userId);

    boolean existsByPhone(String phoneNumber);
}
