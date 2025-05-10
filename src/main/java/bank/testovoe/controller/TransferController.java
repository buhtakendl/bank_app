package bank.testovoe.controller;

import bank.testovoe.dto.transfer.TransferRequest;
import bank.testovoe.dto.transfer.TransferResponse;
import bank.testovoe.mapper.TransferMapper;
import bank.testovoe.model.User;
import bank.testovoe.service.TransferService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
@Tag(name = "Переводы", description = "Переводы между своими картами")
@SecurityRequirement(name = "JWT")
public class TransferController {

    private final TransferService transferService;
    private final TransferMapper transferMapper;

    @PostMapping
    @Operation(summary = "Совершить перевод", description = "Перевод средств между своими активными картами")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Перевод выполнен успешно"),
        @ApiResponse(responseCode = "400", description = "Недостаточно средств или неверные данные"),
        @ApiResponse(responseCode = "403", description = "Одна из карт заблокирована")
    })
    public ResponseEntity<TransferResponse> transfer(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request
    ) {
        var transfer = transferService.transfer(user, request.fromCardNumber(), request.toCardNumber(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(transferMapper.toDto(transfer));
    }
}

