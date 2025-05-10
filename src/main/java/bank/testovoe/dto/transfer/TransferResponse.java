package bank.testovoe.dto.transfer;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransferResponse(
        Long id,
        String fromCardNumber,
        String toCardNumber,
        BigDecimal amount,
        LocalDateTime timestamp
) {
}
