package bank.testovoe.controller;

import bank.testovoe.dto.card.CardDto;
import bank.testovoe.dto.card.CreateCardRequest;
import bank.testovoe.mapper.CardMapper;
import bank.testovoe.model.Card;
import bank.testovoe.model.CardStatus;
import bank.testovoe.model.User;
import bank.testovoe.service.CardService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
@Tag(name = "Карты", description = "Управление банковскими картами пользователя")
@SecurityRequirement(name = "JWT")
@PreAuthorize("hasRole('USER')")
public class CardController {

    private final CardService cardService;
    private final CardMapper cardMapper;

    @GetMapping
    @Operation(summary = "Получить список карт", description = "Возвращает все карты, принадлежащие текущему пользователю")
    public ResponseEntity<List<CardDto>> getCards(
            @AuthenticationPrincipal User user
    ) {
        var cards = cardService.getCardsForUser(user).stream()
                .map(cardMapper::toDto)
                .toList();
        return ResponseEntity.ok(cards);
    }

    @PostMapping
    @Operation(summary = "Создать карту", description = "Создает новую банковскую карту для текущего пользователя")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Карта создана"),
        @ApiResponse(responseCode = "400", description = "Ошибка валидации")
    })
    public ResponseEntity<CardDto> createCard(
            Authentication authentication,
            @Valid @RequestBody CreateCardRequest request
    ) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new AccessDeniedException("Authentication required");
        }
        log.info("Creating card for user {}", user.getEmail());
        Card card = cardService.createCard(user, request.cardNumber(), request.expiryDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDto(card));
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Заблокировать карту", description = "Пользователь запрашивает блокировку своей карты")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
            @ApiResponse(responseCode = "403", description = "Карта уже заблокирована или доступ запрещён")
    })
    public ResponseEntity<Void> blockCard(
            @AuthenticationPrincipal User user,
            @RequestParam String cardNumber) {
        cardService.blockCard(cardNumber, user);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("deposit")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "Пополнить баланс карты",
            description = "Позволяет пользователю пополнить свою карту по её номеру"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно пополнен"),
            @ApiResponse(responseCode = "403", description = "Карта заблокирована или не принадлежит пользователю"),
            @ApiResponse(responseCode = "404", description = "Карта не найдена"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры")
    })
    public ResponseEntity<Void> deposit(
            @AuthenticationPrincipal User user,

            @Parameter(description = "Номер карты (16 цифр)", required = true, example = "6171053773368137")
            @RequestParam
            @Pattern(regexp = "\\d{16}", message = "Номер карты должен состоять из 16 цифр")
            String cardNumber,

            @Parameter(description = "Сумма пополнения", required = true, example = "100.00")
            @RequestParam
            @DecimalMin(value = "0.01", inclusive = true, message = "Сумма пополнения должна быть больше 0")
            BigDecimal amount
    ) {

        cardService.deposit(cardNumber, amount, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск карт пользователя с фильтрами и пагинацией")
    public ResponseEntity<Page<CardDto>> searchCards(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) BigDecimal minBalance,
            @RequestParam(required = false) BigDecimal maxBalance,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<CardDto> page = cardService.searchCards(user, status, minBalance, maxBalance, pageable)
                .map(cardMapper::toDto);

        return ResponseEntity.ok(page);
    }

}

