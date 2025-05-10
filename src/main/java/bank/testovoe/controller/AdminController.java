package bank.testovoe.controller;

import bank.testovoe.dto.card.CardDto;
import bank.testovoe.dto.card.CreateCardRequest;
import bank.testovoe.mapper.CardMapper;
import bank.testovoe.model.Card;
import bank.testovoe.model.User;
import bank.testovoe.service.CardService;
import bank.testovoe.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Админ: Карты", description = "Администрирование банковских карт")
@SecurityRequirement(name = "JWT")
public class AdminController {

    private final CardService cardService;
    private final CardMapper cardMapper;
    private final UserService userService;

    @GetMapping
    @Operation(summary = "Получить все карты в системе")
    public ResponseEntity<List<CardDto>> getAllCards(
            @AuthenticationPrincipal User user
    ) {
        var cards = cardService.getCardsForUser(user).stream()
                .map(cardMapper::toDto)
                .toList();
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @Operation(summary = "Создать карту для пользователя по ID")
    public ResponseEntity<CardDto> createCardForUser(
            @RequestParam Long userId,
            @Valid @RequestBody CreateCardRequest request
    ) {
        User owner = userService.findById(userId);
        Card card = cardService.createCard(owner, request.cardNumber(), request.expiryDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(card));
    }

    @Operation(
            summary = "Активировать карту по номеру",
            description = "Администратор активирует карту по её номеру (16 цифр)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно активирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта уже активна")
    })
    @PostMapping("/activate")
    public ResponseEntity<Void> activateCardByNumber(
            @RequestParam
            @Pattern(regexp = "\\d{16}", message = "Card number must be exactly 16 digits")
            @Parameter(description = "Номер карты (16 цифр)", example = "6171053773368137")
            String cardNumber
    ) {
        cardService.activateCardByNumber(cardNumber);
        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить карту по ID")
    public ResponseEntity<Void> deleteCard(
            @RequestParam
            @Parameter(description = "Номер карты (16 цифр)", example = "1234567890123456")
            @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
            String cardNumber
    ) {
        cardService.deleteCard(cardNumber);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Заблокировать карту по номеру",
            description = "Администратор блокирует карту по её номеру (16 цифр)",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта успешно заблокирована"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "403", description = "Карта уже заблокирована")
    })
    @PostMapping("/block")
    public ResponseEntity<Void> blockCardByAdmin(
            @Pattern(regexp = "\\d{16}", message = "Номер карты должен состоять из 16 цифр")
            @Parameter(description = "Номер карты (16 цифр)", example = "6171053773368137")

            @RequestParam String cardNumber
    ) {
        cardService.blockAnyCard(cardNumber);
        return ResponseEntity.ok().build();
    }
}

