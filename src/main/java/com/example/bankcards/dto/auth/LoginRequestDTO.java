package com.example.bankcards.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    @Email(message = "Incorrect email")
    @NotBlank(message = "Email must not be empty")
    private String email;
    private String password;
}
