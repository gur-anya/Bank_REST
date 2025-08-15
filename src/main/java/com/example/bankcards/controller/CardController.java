package com.example.bankcards.controller;

import com.example.bankcards.dto.card.*;
import com.example.bankcards.security.UserDetailsImpl;
import com.example.bankcards.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Tag(name = "Банковские карты", description = "API для управления банковскими картами")
@SecurityRequirement(name = "bearerAuth")
public class CardController {
    private final CardService cardService;

    @Operation(summary = "Получить все карты пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Список карт получен"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<GetAllCardsResponseDTO> getAllCards(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable,
        CardFilterDTO filterDTO) {

        Long userId = userDetails.getId();
        GetAllCardsResponseDTO response = cardService.getAllCardsByUser(userId, pageable, filterDTO);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Создать новую карту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Карта создана"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDTO> createCard(@Valid @RequestBody CreateCardRequestDTO requestDTO) {
        CardDTO card = cardService.createCard(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }

    @Operation(summary = "Заблокировать карту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Карта заблокирована"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "404", description = "Карта не найдена"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{cardId}/block")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<CardDTO> blockCard(
        @PathVariable Long cardId,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long userId = userDetails.getId();
        CardDTO card = cardService.blockCard(cardId, userId);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Активировать карту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Карта активирована"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "404", description = "Карта не найдена"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{cardId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDTO> activateCard(@PathVariable Long cardId) {
        CardDTO card = cardService.activateCard(cardId);
        return ResponseEntity.ok(card);
    }

    @Operation(summary = "Удалить карту")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Карта удалена"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "404", description = "Карта не найдена"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Установить точный баланс карты (только для ADMIN)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Баланс успешно установлен"),
        @ApiResponse(responseCode = "400", description = "Некорректные данные запроса (например, отрицательный баланс)"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Доступ запрещен (роль не ADMIN)"),
        @ApiResponse(responseCode = "404", description = "Карта не найдена"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PatchMapping("/{cardId}/balance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDTO> setCardBalance(
        @PathVariable Long cardId,
        @Valid @RequestBody SetBalanceRequestDTO requestDTO) {

        CardDTO updatedCard = cardService.setBalance(cardId, requestDTO.getNewBalance());
        return ResponseEntity.ok(updatedCard);
    }
}
