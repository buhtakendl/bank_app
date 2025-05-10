package bank.testovoe.service.impl;

import bank.testovoe.config.CardSpecification;
import bank.testovoe.exception.ForbiddenOperationException;
import bank.testovoe.exception.NotFoundException;
import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.Role;
import bank.testovoe.model.User;
import bank.testovoe.repository.CardRepository;
import bank.testovoe.service.CardService;
import bank.testovoe.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final EncryptionService encryptionService;

    @Override
    public List<Card> getCardsForUser(User user) {
        if (user.getRole() == Role.ADMIN) {
            return cardRepository.findAll();
        }
        return cardRepository.findByOwner(user);
    }

    @Override
    public Card getByIdAndUser(Long cardId, User user) {
        return cardRepository.findByIdAndOwner(cardId, user)
                .orElseThrow(() -> new NotFoundException("Card not found or access denied"));
    }

    @Override
    @Transactional
    public Card createCard(User owner, String cardNumber, LocalDate expiryDate) {
        log.info("Creating card for user {} with expiry {}", owner.getEmail(), expiryDate);
        String encrypted = encryptionService.encrypt(cardNumber);

        if (cardRepository.existsByEncryptedCardNumber(encrypted)) {
            throw new ForbiddenOperationException("Card already exists");
        }

        Card card = Card.builder()
                .owner(owner)
                .encryptedCardNumber(encrypted)
                .expiryDate(expiryDate)
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.ZERO)
                .build();
        return cardRepository.save(card);
    }

    @Override
    @Transactional
    public void blockCard(String cardNumber, User user) {
        String encrypted = encryptionService.encrypt(cardNumber);
        Card card = cardRepository.findByEncryptedCardNumberAndOwner(encrypted, user)
                .orElseThrow(() -> new NotFoundException("Card not found or access denied"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ForbiddenOperationException("Card already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void blockAnyCard(String cardNumber) {
        String encrypted = encryptionService.encrypt(cardNumber);
        Card card = cardRepository.findByEncryptedCardNumber(encrypted)
                .orElseThrow(() -> new NotFoundException("Card not found"));

        log.info("Admin requested to block card: {}, owned by: {}", cardNumber, card.getOwner().getEmail());

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ForbiddenOperationException("Card already blocked");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void activateCardByNumber(String cardNumber) {
         log.info("Activating card with number {}", cardNumber);
         String encrypted = encryptionService.encrypt(cardNumber);
         Card card = cardRepository.findByEncryptedCardNumber(encrypted)
                 .orElseThrow(() -> new NotFoundException("Card not found with number: " + cardNumber));
         if(card.getStatus() == CardStatus.ACTIVE) {
             throw new ForbiddenOperationException("Card already active");
         }
         card.setStatus(CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void deleteCard(String cardNumber) {
        log.warn("Deleting card {}", cardNumber);
        String encrypted = encryptionService.encrypt(cardNumber);

        Card card = cardRepository.findByEncryptedCardNumber(encrypted)
                .orElseThrow(() -> new NotFoundException("Card not found"));
        cardRepository.delete(card);
    }

    @Override
    @Transactional
    public void deposit(String cardNumber, BigDecimal amount, User user) {

        String encrypted = encryptionService.encrypt(cardNumber);
        Card card = cardRepository.findByEncryptedCardNumberAndOwner(encrypted, user)
                .orElseThrow(() -> new NotFoundException("Card not found or does not belong to user"));
        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new ForbiddenOperationException("Card already blocked");
        }
        card.setBalance(card.getBalance().add(amount));
        cardRepository.save(card);
    }

    @Override
    public Card findById(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Card not found with id: " + id));
    }

    public Page<Card> searchCards(User user, CardStatus status, BigDecimal minBalance, BigDecimal maxBalance, Pageable pageable) {
    Specification<Card> spec = Specification.where(CardSpecification.ownerIs(user));

    if (status != null) {
        spec = spec.and(CardSpecification.statusIs(status));
    }
    if (minBalance != null) {
        spec = spec.and(CardSpecification.balanceGreaterThanOrEqual(minBalance));
    }
    if (maxBalance != null) {
        spec = spec.and(CardSpecification.balanceLessThanOrEqual(maxBalance));
    }

    return cardRepository.findAll(spec, pageable);
}

}
