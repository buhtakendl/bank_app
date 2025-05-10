package bank.testovoe.service;

import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardService {
    List<Card> getCardsForUser(User user);
    Card getByIdAndUser(Long cardId, User user);
    Card createCard(User owner, String cardNumber, LocalDate expiryDate);
    void blockCard(String cardNumber, User user);
    void activateCardByNumber(String cardNumber);
    void deleteCard(String cardNumber);
    void blockAnyCard(String cardNumber);
    void deposit(String cardId, BigDecimal amount, User user);
    Card findById(Long id);
    Page<Card> searchCards(User user, CardStatus status, BigDecimal minBalance, BigDecimal maxBalance, Pageable pageable);
}
