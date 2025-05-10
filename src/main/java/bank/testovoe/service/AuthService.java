package bank.testovoe.service;

import bank.testovoe.model.User;

public interface AuthService {

    String login(String email, String password);
    User register(String email, String password);
}
