package bank.testovoe.dto.card;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record CreateCardRequest(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String cardNumber,

        @NotNull(message = "Expiry date is required")
        LocalDate expiryDate
) {
}
