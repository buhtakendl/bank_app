package bank.testovoe.service.impl;

import bank.testovoe.exception.ForbiddenOperationException;
import bank.testovoe.exception.NotFoundException;
import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.Role;
import bank.testovoe.model.User;
import bank.testovoe.repository.CardRepository;
import bank.testovoe.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User user;
    private User admin;
    private Card card;
    private final String cardNumber = "1234567890123456";
    private final String encryptedCardNumber = "encrypted1234567890123456";

    @BeforeEach
    void setUp() {
        user = User.builder().email("user@test.com").role(Role.USER).build();
        admin = User.builder().email("admin@test.com").role(Role.ADMIN).build();
        card = Card.builder()
                .id(1L)
                .owner(user)
                .encryptedCardNumber(encryptedCardNumber)
                .expiryDate(LocalDate.now().plusYears(3))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
    }

    @Test
    void getCardsForUser_UserRole_ReturnsUserCards() {
        when(cardRepository.findByOwner(user)).thenReturn(List.of(card));

        List<Card> result = cardService.getCardsForUser(user);

        assertEquals(1, result.size());
        assertEquals(card, result.get(0));
        verify(cardRepository).findByOwner(user);
    }

    @Test
    void getCardsForUser_AdminRole_ReturnsAllCards() {
        when(cardRepository.findAll()).thenReturn(List.of(card));

        List<Card> result = cardService.getCardsForUser(admin);

        assertEquals(1, result.size());
        assertEquals(card, result.get(0));
        verify(cardRepository).findAll();
    }

    @Test
    void getByIdAndUser_CardExists_ReturnsCard() {
        when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(card));

        Card result = cardService.getByIdAndUser(1L, user);

        assertEquals(user, result.getOwner());
        assertEquals(CardStatus.ACTIVE, result.getStatus());

        verify(cardRepository).findByIdAndOwner(1L, user);
    }

    @Test
    void getByIdAndUser_CardNotFound_ThrowsNotFoundException() {
        when(cardRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.getByIdAndUser(1L, user));
        verify(cardRepository).findByIdAndOwner(1L, user);
    }

    @Test
    void createCard_NewCard_SuccessfullyCreated() {
        LocalDate expiryDate = LocalDate.now().plusYears(3);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)).thenReturn(false);
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.createCard(user, cardNumber, expiryDate);

        assertEquals(user, result.getOwner());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).existsByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    void createCard_CardExists_ThrowsForbiddenOperationException() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)).thenReturn(true);

        assertThrows(ForbiddenOperationException.class, () -> cardService.createCard(user, cardNumber, LocalDate.now()));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).existsByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_CardExists_SuccessfullyBlocked() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumberAndOwner(encryptedCardNumber, user)).thenReturn(Optional.of(card));

        cardService.blockCard(cardNumber, user);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumberAndOwner(encryptedCardNumber, user);
        verify(cardRepository).save(card);
    }

    @Test
    void blockCard_CardNotFound_ThrowsNotFoundException() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumberAndOwner(encryptedCardNumber, user)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.blockCard(cardNumber, user));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumberAndOwner(encryptedCardNumber, user);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_AlreadyBlocked_ThrowsForbiddenOperationException() {
        card.setStatus(CardStatus.BLOCKED);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumberAndOwner(encryptedCardNumber, user)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenOperationException.class, () -> cardService.blockCard(cardNumber, user));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumberAndOwner(encryptedCardNumber, user);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockAnyCard_CardExists_SuccessfullyBlocked() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(card));

        cardService.blockAnyCard(cardNumber);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository).save(card);
    }

    @Test
    void blockAnyCard_CardNotFound_ThrowsNotFoundException() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.blockAnyCard(cardNumber));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCardByNumber_CardExists_SuccessfullyActivated() {
        card.setStatus(CardStatus.BLOCKED);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(card));

        cardService.activateCardByNumber(cardNumber);

        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository).save(card);
    }

    @Test
    void activateCardByNumber_AlreadyActive_ThrowsForbiddenOperationException() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenOperationException.class, () -> cardService.activateCardByNumber(cardNumber));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_CardExists_SuccessfullyDeleted() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.of(card));

        cardService.deleteCard(cardNumber);

        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository).delete(card);
    }

    @Test
    void deleteCard_CardNotFound_ThrowsNotFoundException() {
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumber(encryptedCardNumber)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.deleteCard(cardNumber));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumber(encryptedCardNumber);
        verify(cardRepository, never()).delete(any(Card.class));
    }

    @Test
    void deposit_CardExists_SuccessfullyDeposited() {
        BigDecimal amount = new BigDecimal("100.00");
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumberAndOwner(encryptedCardNumber, user)).thenReturn(Optional.of(card));

        cardService.deposit(cardNumber, amount, user);

        assertEquals(amount, card.getBalance());
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumberAndOwner(encryptedCardNumber, user);
        verify(cardRepository).save(card);
    }

    @Test
    void deposit_CardBlocked_ThrowsForbiddenOperationException() {
        BigDecimal amount = new BigDecimal("100.00");
        card.setStatus(CardStatus.BLOCKED);
        when(encryptionService.encrypt(cardNumber)).thenReturn(encryptedCardNumber);
        when(cardRepository.findByEncryptedCardNumberAndOwner(encryptedCardNumber, user)).thenReturn(Optional.of(card));

        assertThrows(ForbiddenOperationException.class, () -> cardService.deposit(cardNumber, amount, user));
        verify(encryptionService).encrypt(cardNumber);
        verify(cardRepository).findByEncryptedCardNumberAndOwner(encryptedCardNumber, user);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void findById_CardExists_ReturnsCard() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        Card result = cardService.findById(1L);

        assertEquals(user, result.getOwner());
        assertEquals(CardStatus.ACTIVE, result.getStatus());

        verify(cardRepository).findById(1L);
    }

    @Test
    void findById_CardNotFound_ThrowsNotFoundException() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cardService.findById(1L));
        verify(cardRepository).findById(1L);
    }

    @Test
    void searchCards_WithFilters_ReturnsPagedCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Card> result = cardService.searchCards(user, CardStatus.ACTIVE, BigDecimal.ZERO, new BigDecimal("1000"), pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(card, result.getContent().get(0));
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void searchCards_NoFilters_ReturnsPagedCards() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Card> result = cardService.searchCards(user, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(card, result.getContent().get(0));
        verify(cardRepository).findAll(any(Specification.class), eq(pageable));
    }
}