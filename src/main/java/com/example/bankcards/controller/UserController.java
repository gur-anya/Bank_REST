package com.example.bankcards.controller;

import com.example.bankcards.dto.MessageResponseDTO;
import com.example.bankcards.dto.user.GetAllUsersResponseDTO;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Пользователи", description = "API для администратора: управление пользователями")
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;


    @Operation(
        summary = "Удаляет пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Успешное удаление - пустой ответ"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Блокирует пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная блокировка"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/block/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("User " + userId + " blocked successfully."));

    }

    @Operation(
        summary = "Разблокирует пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешная разблокировка"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping("/unblock/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> unblockUser(@PathVariable Long userId) {
        userService.unblockUser(userId);
        return ResponseEntity.ok(new MessageResponseDTO("User " + userId + " unblocked successfully."));
    }

    @Operation(
        summary = "Получает всех пользователей")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный ответ со списком всех пользователей"),
        @ApiResponse(responseCode = "401", description = "Не авторизован"),
        @ApiResponse(responseCode = "403", description = "Нет прав"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GetAllUsersResponseDTO> getAllUsers(Pageable pageable) {
        GetAllUsersResponseDTO usersResponseDTO = userService.getAllUsers(pageable);
        return ResponseEntity.ok(usersResponseDTO);
    }
}
