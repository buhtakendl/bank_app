package bank.testovoe.dto.card;

import bank.testovoe.model.CardStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CardDto(
        Long id,
        String maskedCardNumber,
        LocalDate expiryDate,
        CardStatus status,
        BigDecimal balance) {
}
