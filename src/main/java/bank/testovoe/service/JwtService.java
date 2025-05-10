package bank.testovoe.service;

import bank.testovoe.model.User;

public interface JwtService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token, User user);
}
