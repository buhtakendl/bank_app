package bank.testovoe.service.impl;

import bank.testovoe.exception.ForbiddenOperationException;
import bank.testovoe.exception.InsufficientFundsException;
import bank.testovoe.exception.NotFoundException;
import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.Transfer;
import bank.testovoe.model.User;
import bank.testovoe.repository.CardRepository;
import bank.testovoe.repository.TransferRepository;
import bank.testovoe.service.EncryptionService;
import bank.testovoe.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final CardRepository cardRepository;
    private final TransferRepository transferRepository;
    private final EncryptionService encryptionService;

    @Override
    @Transactional
    public Transfer transfer(User user, String fromCardNumber, String toCardNumber, BigDecimal amount) {
        log.info("User {} initiates transfer from card {} to card {} ({} RUB)",
                user.getEmail(), fromCardNumber, toCardNumber, amount);

        if (fromCardNumber.equals(toCardNumber)) {
            throw new ForbiddenOperationException("Cannot transfer to the same card");
        }

        String encryptedFrom = encryptionService.encrypt(fromCardNumber);
        String encryptedTo = encryptionService.encrypt(toCardNumber);


        Card from = cardRepository.findByEncryptedCardNumberAndOwner(encryptedFrom, user)
                .orElseThrow(() -> new NotFoundException("Source card not found"));

        Card to = cardRepository.findByEncryptedCardNumberAndOwner(encryptedTo, user)
                .orElseThrow(() -> new NotFoundException("Destination card not found"));

        if (from.getStatus() != CardStatus.ACTIVE || to.getStatus() != CardStatus.ACTIVE) {
            throw new ForbiddenOperationException("Both cards must be active");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        if (!from.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenOperationException("You can transfer only from your own card");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transfer transfer = Transfer.builder()
                .fromCard(from)
                .toCard(to)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build();

        log.info("Transfer successful: {} -> {} | amount: {}", fromCardNumber, toCardNumber, amount);
        return transferRepository.save(transfer);
    }
}
