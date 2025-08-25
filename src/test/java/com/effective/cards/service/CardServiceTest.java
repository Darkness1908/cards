package com.effective.cards.service;

import com.effective.cards.dto.CardStatusUpdate;
import com.effective.cards.dto.TransactionMoney;
import com.effective.cards.enums.CardStatus;
import com.effective.cards.model.Card;
import com.effective.cards.model.User;
import com.effective.cards.repository.CardRepository;
import com.effective.cards.repository.UserRepository;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class CardServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private StandardPBEStringEncryptor cardEncryptor;

    @InjectMocks
    private CardService cardService;

    private String cardNumber;
    private String phoneNumber;
    private User user;
    private Card card;

    @BeforeEach
    public void setUp() {
        cardNumber = "1234567890123456";
        phoneNumber = "89040953350";
        user = new User();
        card = new Card();
        user.setPhone(phoneNumber);
        user.setId(1L);
        card.setId(1L);
        card.setOwner(user);
    }

    @Test
    public void testCreateCard_UserNotFound() {
        when(userRepository.findByPhone(phoneNumber)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createCard(phoneNumber));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    public void testCreateCard_Success() {
        when(userRepository.findByPhone(phoneNumber)).thenReturn(Optional.of(user));
        when(cardRepository.existsByEncryptedCardNumber(anyString())).thenReturn(true).thenReturn(false);
        when(cardEncryptor.encrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
        when(cardEncryptor.decrypt(anyString())).thenAnswer(invocation -> invocation.getArgument(0));

        cardService.createCard(phoneNumber);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card savedCard = cardCaptor.getValue();

        assertEquals((savedCard.getEncryptedCardNumber()).substring(12),
                savedCard.getLast4Digits());
        assertEquals(user, savedCard.getOwner());
        assertTrue(isValidLuhn(savedCard.getEncryptedCardNumber()));
    }

    @Test
    public void testDeleteCard_Success() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);

        when(cardRepository.findByEncryptedCardNumber(cardNumber))
                .thenReturn(Optional.of(card));

        cardService.deleteCard(cardNumber);
        verify(cardRepository).delete(card);
    }

    @Test
    public void testDeleteCard_CardNotFound() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);

        when(cardRepository.findByEncryptedCardNumber(cardNumber))
                .thenReturn(Optional.empty());

        cardService.deleteCard(cardNumber);
        verify(cardRepository, never()).delete(any());
    }

    @Test
    public void testChangeCardStatus_CardNotFound() {
        CardStatusUpdate cardStatusUpdate = new CardStatusUpdate(cardNumber, CardStatus.ACTIVE);

        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.changeCardStatus(cardStatusUpdate));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    @Test
    public void testChangeCardStatus_UpdatedStatusIsExpired() {
        CardStatusUpdate cardStatusUpdate = new CardStatusUpdate(cardNumber, CardStatus.EXPIRED);
        card.setStatus(CardStatus.ACTIVE);
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.changeCardStatus(cardStatusUpdate));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("You can't set status \"EXPIRED\"", exception.getReason());
    }

    @Test
    public void testChangeCardStatus_CardIsExpired() {
        CardStatusUpdate cardStatusUpdate = new CardStatusUpdate(cardNumber, CardStatus.ACTIVE);
        card.setStatus(CardStatus.EXPIRED);
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.changeCardStatus(cardStatusUpdate));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card is expired", exception.getReason());
    }

    @Test
    public void testChangeCardStatus_SuccessWhenSetStatusBlocked() {
        CardStatusUpdate cardStatusUpdate = new CardStatusUpdate(cardNumber, CardStatus.BLOCKED);
        card.setStatus(CardStatus.ACTIVE);
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        cardService.changeCardStatus(cardStatusUpdate);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card savedCard = cardCaptor.getValue();

        assertEquals(CardStatus.BLOCKED, savedCard.getStatus());
    }

    @Test
    public void testChangeCardStatus_SuccessWhenSetStatusActive() {
        CardStatusUpdate cardStatusUpdate = new CardStatusUpdate(cardNumber, CardStatus.ACTIVE);
        card.setStatus(CardStatus.BLOCKED);
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        cardService.changeCardStatus(cardStatusUpdate);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card savedCard = cardCaptor.getValue();

        assertEquals(CardStatus.ACTIVE, savedCard.getStatus());
    }

    @Test
    public void testSendBlockRequest_CardNotFound() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.sendBlockRequest(cardNumber, anyLong()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void testSendBlockRequest_SomeoneElsesCard() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.sendBlockRequest(cardNumber, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You can't send block request for someone else's card", exception.getReason());
    }

    @Test
    public void testSendBlockRequest_InactiveCard() {
        card.setStatus(CardStatus.EXPIRED);
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.sendBlockRequest(cardNumber, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Card is blocked or already expired", exception.getReason());
    }

    @Test
    public void testSendBlockRequest_Success() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        cardService.sendBlockRequest(cardNumber, 1L);

        ArgumentCaptor<Card> cardCaptor = ArgumentCaptor.forClass(Card.class);
        verify(cardRepository).save(cardCaptor.capture());
        Card savedCard = cardCaptor.getValue();

        assertTrue(savedCard.isBlockRequested());
    }

    @Test
    public void testGetBalance_CardNotFound() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.getBalance(cardNumber, anyLong()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Card not found", exception.getReason());
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void testGetBalance_SomeoneElsesCard() {
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.getBalance(cardNumber, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You can't check balance of someone else's card", exception.getReason());
    }

    @Test
    public void testGetBalance_Success() {
        card.setBalance(new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(cardNumber)).thenReturn(cardNumber);
        when(cardRepository.findByEncryptedCardNumber(cardNumber)).thenReturn(Optional.of(card));

        assertEquals("100.00", cardService.getBalance(cardNumber, 1L).toString());
    }

    @Test
    public void testCreateTransaction_firstCardNotFound() {
        String otherCardNumber = "4276070019084120";
        Card otherCard = new Card();
        otherCard.setEncryptedCardNumber(otherCardNumber);
        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.empty())
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, anyLong()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Sender's card not found", exception.getReason());
    }

    @Test
    public void testCreateTransaction_secondCardNotFound() {
        String otherCardNumber = "4276070019084120";
        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, anyLong()));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Recipient's card not found", exception.getReason());
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void testCreateTransaction_SomeoneElsesFirstCard() {
        Card otherCard = new Card();
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 2L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You can't do transaction from someone else's card", exception.getReason());
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void testCreateTransaction_SomeoneElsesSecondCard() {
        User otherUser = new User();
        otherUser.setId(2L);

        Card otherCard = new Card();
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(otherUser);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("You can't do transaction to someone else's card", exception.getReason());
    }

    @Test
    public void testCreateTransaction_CardAreEquals() {
        User otherUser = new User();
        otherUser.setId(1L);

        Card otherCard = new Card();
        otherCard.setId(1L);
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(otherUser);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 1L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Cards are equals", exception.getReason());
    }

    @Test
    public void testCreateTransaction_FirstCardIsInactive() {
        card.setStatus(CardStatus.BLOCKED);
        Card otherCard = new Card();
        otherCard.setId(2L);
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(user);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("Card you want to debit funds from is blocked or already expired", exception.getReason());
    }

    @Test
    public void testCreateTransaction_SecondCardIsInactive() {
        Card otherCard = new Card();
        otherCard.setStatus(CardStatus.EXPIRED);
        otherCard.setId(2L);
        card.setStatus(CardStatus.ACTIVE);
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(user);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));
        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 1L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        assertEquals("The card you want to top up is blocked or already expired", exception.getReason());
    }

    @Test
    public void testCreateTransaction_NotEnoughFunds() {
        Card otherCard = new Card();
        otherCard.setId(2L);
        otherCard.setStatus(CardStatus.ACTIVE);
        card.setStatus(CardStatus.ACTIVE);
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(user);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));

        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));
        when(cardRepository.withdraw(card.getId(), transactionMoney.sum())).thenReturn(0);


        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> cardService.createTransaction(transactionMoney, 1L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals("Insufficient funds", exception.getReason());
    }

    @Test
    public void testCreateTransaction_Success() {
        Card otherCard = new Card();
        otherCard.setId(2L);
        otherCard.setStatus(CardStatus.ACTIVE);
        card.setStatus(CardStatus.ACTIVE);
        String otherCardNumber = "4276070019084120";
        otherCard.setEncryptedCardNumber(otherCardNumber);
        otherCard.setOwner(user);

        TransactionMoney transactionMoney = new TransactionMoney(cardNumber, otherCardNumber, new BigDecimal("100.00"));

        when(cardEncryptor.encrypt(anyString())).thenReturn(cardNumber).thenReturn(otherCardNumber);
        when(cardRepository.findByEncryptedCardNumber(anyString())).thenReturn(Optional.of(card))
                .thenReturn(Optional.of(otherCard));
        when(cardRepository.withdraw(card.getId(), transactionMoney.sum())).thenReturn(1);

        cardService.createTransaction(transactionMoney, 1L);
        verify(cardRepository).deposit(otherCard.getId(), transactionMoney.sum());
    }

    @SuppressWarnings("SpellCheckingInspection")
    private boolean isValidLuhn(String number) {
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
        return (sum % 10) == 0;
    }
}
