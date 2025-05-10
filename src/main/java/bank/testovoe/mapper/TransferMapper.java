package bank.testovoe.mapper;

import bank.testovoe.dto.transfer.TransferResponse;
import bank.testovoe.model.Transfer;
import bank.testovoe.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferMapper {

    private final EncryptionService encryptionService;
    private final CardMapper cardMapper;

    public TransferResponse toDto(Transfer transfer) {
        String fromDecrypted = encryptionService.decrypt(transfer.getFromCard().getEncryptedCardNumber());
        String toDecrypted = encryptionService.decrypt(transfer.getToCard().getEncryptedCardNumber());

        return TransferResponse.builder()
                .id(transfer.getId())
                .fromCardNumber(cardMapper.maskCardNumber(fromDecrypted))
                .toCardNumber(cardMapper.maskCardNumber(toDecrypted))
                .amount(transfer.getAmount())
                .timestamp(transfer.getTimestamp())
                .build();
    }
}
