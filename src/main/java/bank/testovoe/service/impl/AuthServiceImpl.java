package bank.testovoe.service.impl;

import bank.testovoe.exception.ForbiddenOperationException;
import bank.testovoe.exception.NotFoundException;
import bank.testovoe.model.Role;
import bank.testovoe.model.User;
import bank.testovoe.service.AuthService;
import bank.testovoe.service.JwtService;
import bank.testovoe.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public String login(String email, String password) {
        log.info("Login attempt for email: {}", email);
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ForbiddenOperationException("Invalid email or password");
        }

        return jwtService.generateToken(user);
    }

    @Override
    public User register(String email, String password) {
        log.info("Registering new user with email: {}", email);
        if (userService.existsByEmail(email)) {
            throw new ForbiddenOperationException("Email already in use");
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.USER)
                .build();

        return userService.save(user);
    }
}
