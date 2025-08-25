package com.effective.cards.repository;

import com.effective.cards.model.Card;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    boolean existsByEncryptedCardNumber(String cardNumber);
    Optional<Card> findByEncryptedCardNumber(String cardNumber);

    @Query("SELECT c FROM Card c")
    Page<Card> findCardsByPage(Pageable pageable);

    @Query("SELECT c FROM Card c WHERE c.blockRequested = :request")
    Page<Card> findCardsByPageAndBlockRequest(Pageable pageable, @Param("request") boolean request);

    @Query("SELECT c FROM Card c WHERE c.owner.id = :ownerId")
    Page<Card> findCardsByPageAndOwnerId(Pageable pageable, @Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("UPDATE Card c SET c.balance = c.balance - :sum WHERE c.id = :cardId AND c.balance >= :sum")
    int withdraw(@Param("cardId") Long cardId, @Param("sum") BigDecimal sum);

    @Modifying
    @Transactional
    @Query("UPDATE Card c SET c.balance = c.balance + :sum WHERE c.id = :cardId")
    void deposit(@Param("cardId") Long cardId, @Param("sum") BigDecimal sum);
}
