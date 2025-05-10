package bank.testovoe.mapper;

import bank.testovoe.dto.card.CardDto;
import bank.testovoe.model.Card;
import bank.testovoe.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardMapper {

    private final EncryptionService encryptionService;

    public CardDto toDto(Card card) {
        String decrypted = encryptionService.decrypt(card.getEncryptedCardNumber());
        String masked = maskCardNumber(decrypted);

        return CardDto.builder()
                .id(card.getId())
                .maskedCardNumber(masked)
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .build();
    }

    public String maskCardNumber(String cardNumber) {
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }
}
