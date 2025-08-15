package com.example.bankcards.controller;

import com.example.bankcards.dto.transaction.CreateTransactionRequestDTO;
import com.example.bankcards.dto.transaction.TransactionDTO;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Транзакции", description = "API для управления транзакциями между картами")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Создать транзакцию между картами")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Транзакция создана"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "404", description = "Карта не найдена"),
        @ApiResponse(responseCode = "400", description = "Недостаточно средств или карта заблокирована"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<TransactionDTO> createTransaction(
        @Valid @RequestBody CreateTransactionRequestDTO requestDTO,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();
        TransactionDTO transaction = transactionService.createTransaction(requestDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
    }

    @Operation(summary = "Получить транзакции пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список транзакций получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<TransactionDTO>> getTransactions(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {

        Long userId = userDetails.getId();
        Page<TransactionDTO> transactions = transactionService.getTransactionsByUser(userId, pageable);
        return ResponseEntity.ok(transactions);
    }
}
