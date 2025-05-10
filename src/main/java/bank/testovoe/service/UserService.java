package bank.testovoe.service;

import bank.testovoe.model.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);
    User save(User user);
    boolean existsByEmail(String email);
    User findById(Long id);
}
