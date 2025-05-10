package bank.testovoe.controller;

import bank.testovoe.dto.user.UserDto;
import bank.testovoe.mapper.UserMapper;
import bank.testovoe.model.User;
import bank.testovoe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "JWT")
@Tag(name = "User", description = "Управление пользователями")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "Получить пользователя по ID (ADMIN)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toDto(user));
    }

    @Operation(summary = "Получить текущего пользователя (USER)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(userMapper.toDto(user));
    }
}

