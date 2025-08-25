package com.effective.cards.model;

import com.effective.cards.enums.CardStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "card_number_encrypted", nullable = false, unique = true)
    private String encryptedCardNumber;

    @Column(name = "last_four_digits", nullable = false)
    private String last4Digits;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "expiration_date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime expirationDate;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "block_request", nullable = false)
    private boolean blockRequested;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardStatus status;

    public Card() {}

    public Card(String encryptedCardNumber, User owner, String last4digits) {
        this.encryptedCardNumber = encryptedCardNumber;
        this.owner = owner;
        this.createdAt = LocalDateTime.now();
        this.expirationDate = LocalDateTime.now().plusYears(4);
        this.status = CardStatus.ACTIVE;
        this.balance = BigDecimal.ZERO;
        this.blockRequested = false;
        this.last4Digits = last4digits;
    }
}
