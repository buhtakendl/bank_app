package bank.testovoe.controller;

import bank.testovoe.dto.auth.LoginRequest;
import bank.testovoe.dto.auth.LoginResponse;
import bank.testovoe.dto.auth.RegisterRequest;
import bank.testovoe.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Регистрация пользователей")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя", description = "Создает нового пользователя с email и паролем")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации или email уже используется")
    })
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        authService.register(request.email(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    @Operation(summary = "Авторизация пользователя", description = "Возвращает JWT токен при успешной аутентификации")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Успешный вход, возвращён токен"),
        @ApiResponse(responseCode = "401", description = "Неверные email или пароль")
    })
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        String token = authService.login(request.email(), request.password());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}

