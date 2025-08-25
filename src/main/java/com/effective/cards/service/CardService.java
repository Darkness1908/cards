package com.effective.cards.service;

import com.effective.cards.dto.CardInfo;
import com.effective.cards.dto.CardStatusUpdate;
import com.effective.cards.dto.MyCardInfo;
import com.effective.cards.dto.TransactionMoney;
import com.effective.cards.enums.CardStatus;
import com.effective.cards.model.Card;
import com.effective.cards.model.User;
import com.effective.cards.repository.CardRepository;
import com.effective.cards.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Random;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CardService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final StandardPBEStringEncryptor cardEncryptor;
    private final Random random = new Random();

    public void createCard(String phoneNumber) {
        User user = userRepository.findByPhone(phoneNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String cardNumber = generateCardNumber();
        String last4Digits = (cardEncryptor.decrypt(cardNumber)).substring(12);
        Card card = new Card(cardNumber, user, last4Digits);
        cardRepository.save(card);
    }

    public void deleteCard(String cardNumber) {
        String encryptedNumber = encryptCardNumber(cardNumber);
        cardRepository.findByEncryptedCardNumber(encryptedNumber).ifPresent(cardRepository::delete);
    }

    public void changeCardStatus(CardStatusUpdate cardStatusUpdate) {
        String encryptedNumber = encryptCardNumber(cardStatusUpdate.cardNumber());
        Card card = cardRepository.findByEncryptedCardNumber(encryptedNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")
        );

        if (cardStatusUpdate.status() == CardStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You can't set status \"EXPIRED\"");
        }

        if (card.getStatus() == CardStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card is expired");
        }

        card.setStatus(cardStatusUpdate.status());
        cardRepository.save(card);
    }

    public List<CardInfo> getCardsInfo(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Card> pageCards = cardRepository.findCardsByPage(pageable);
        List<Card> cards = pageCards.getContent();
        return cardsToDto(cards);
    }

    public List<CardInfo> getCardBlockRequests(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Card> pageCards = cardRepository.findCardsByPageAndBlockRequest(pageable, true);
        List<Card> cards = pageCards.getContent();
        return cardsToDto(cards);
    }

    public List<MyCardInfo> getMyCardsInfo(int page, int pageSize, Long id) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Card> pageCards = cardRepository.findCardsByPageAndOwnerId(pageable, id);
        List<Card> cards = pageCards.getContent();
        return myCardsToDto(cards);
    }

    public void sendBlockRequest(String cardNumber, Long id) {
        String encryptedNumber = encryptCardNumber(cardNumber);
        Card card = cardRepository.findByEncryptedCardNumber(encryptedNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")
        );

        if (!card.getOwner().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't send block request for someone else's card");
        }

        if (card.getStatus() == CardStatus.EXPIRED || card.getStatus() == CardStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Card is blocked or already expired");
        }

        card.setBlockRequested(true);
        cardRepository.save(card);
    }

    public BigDecimal getBalance(String cardNumber, Long id) {
        String encryptedNumber = encryptCardNumber(cardNumber);
        Card card = cardRepository.findByEncryptedCardNumber(encryptedNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card not found")
        );

        if (!card.getOwner().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't check balance of someone else's card");
        }

        return card.getBalance();
    }

    @Transactional
    public void createTransaction(TransactionMoney transactionMoney, Long userId) {
        String encryptedCardNumberFrom = encryptCardNumber(transactionMoney.firstCard());
        String encryptedCardNumberTo = encryptCardNumber(transactionMoney.secondCard());
        Card cardFrom = cardRepository.findByEncryptedCardNumber(encryptedCardNumberFrom)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Sender's card not found"));

        Card cardTo = cardRepository.findByEncryptedCardNumber(encryptedCardNumberTo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Recipient's card not found"));

        if (!cardFrom.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't do transaction from someone else's card");
        }

        if (!cardTo.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can't do transaction to someone else's card");
        }

        if (cardFrom.getId().equals(cardTo.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cards are equals");
        }

        if (cardFrom.getStatus() == CardStatus.EXPIRED || cardFrom.getStatus() == CardStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Card you want to debit funds from is blocked or already expired");
        }

        if (cardTo.getStatus() == CardStatus.EXPIRED || cardTo.getStatus() == CardStatus.BLOCKED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "The card you want to top up is blocked or already expired");
        }

        int updatedRows = cardRepository.withdraw(cardFrom.getId(), transactionMoney.sum());
        if (updatedRows == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }

        cardRepository.deposit(cardTo.getId(), transactionMoney.sum());
    };

    private List<CardInfo> cardsToDto(@NotNull List<Card> cards) {
        return cards.stream()
                .map(CardInfo::new)
                .collect(Collectors.toList());
    }

    private List<MyCardInfo> myCardsToDto(List<Card> cards) {
        return cards.stream()
                .map(MyCardInfo::new)
                .collect(Collectors.toList());
    }

    private String generateCardNumber() {
        String encryptedCardNumber = "";
        do {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                stringBuilder.append(random.nextInt(10));
            }
            String partialNumber = stringBuilder.toString();
            int checkDigit = calculateCheckDigit(partialNumber);
            System.out.println(partialNumber + checkDigit);
            encryptedCardNumber = encryptCardNumber(partialNumber + checkDigit);
        }
        while (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber));
        return encryptedCardNumber;
    }

    private String encryptCardNumber(String cardNumber) {
        return cardEncryptor.encrypt(cardNumber);
    }

    private int calculateCheckDigit(String number) {
        int sum = 0;
        boolean isCheck = true;
        for (int i = 0; i < number.length(); i++) {
            int n = Character.getNumericValue(number.charAt(i));
            if (isCheck) {
                n = n*2;
                if (n > 9) n -= 9;
            }
            sum += n;
            isCheck = !isCheck;
        }
        return (10 - (sum % 10)) % 10;
    }
}
