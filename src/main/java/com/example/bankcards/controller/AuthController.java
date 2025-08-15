package com.example.bankcards.controller;

import com.example.bankcards.dto.auth.CreateUserRequestDTO;
import com.example.bankcards.dto.auth.CreateUserResponseDTO;
import com.example.bankcards.dto.auth.LoginRequestDTO;
import com.example.bankcards.dto.auth.TokenDTO;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.TokenService;
import com.example.bankcards.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "API для регистрации, входа и выхода")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;
    private final TokenService tokenService;

    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
        @ApiResponse(responseCode = "409", description = "Пользователь с таким email уже существует"),
        @ApiResponse(responseCode = "400", description = "Неверные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/signup")
    public ResponseEntity<CreateUserResponseDTO> signup(@Valid @RequestBody CreateUserRequestDTO requestDTO) {
        CreateUserResponseDTO response = userService.createUser(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Вход в систему")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный вход"),
        @ApiResponse(responseCode = "401", description = "Неверные учетные данные"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        TokenDTO token = authService.login(requestDTO);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Выход из системы")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Успешный выход"),
        @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.blacklistToken(token);
        }
        return ResponseEntity.ok().build();
    }
}
