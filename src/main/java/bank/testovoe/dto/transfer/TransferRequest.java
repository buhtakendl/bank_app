package bank.testovoe.dto.transfer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "Source card ID is required")
        String fromCardNumber,

        @NotNull(message = "Destination card ID is required")
        String toCardNumber,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be greater than zero")
        BigDecimal amount
) {
}
