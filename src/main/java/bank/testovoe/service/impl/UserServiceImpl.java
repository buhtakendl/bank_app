package bank.testovoe.service.impl;

import bank.testovoe.exception.NotFoundException;
import bank.testovoe.model.User;
import bank.testovoe.repository.UserRepository;
import bank.testovoe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        log.info("Authenticated name: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public User save(User user) {
    log.debug("Saving user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }
}
