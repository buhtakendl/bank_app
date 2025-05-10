package bank.testovoe.service;

import bank.testovoe.model.Transfer;
import bank.testovoe.model.User;

import java.math.BigDecimal;

public interface TransferService {
        Transfer transfer(User user, String fromCardId, String toCardId, BigDecimal amount);
}
