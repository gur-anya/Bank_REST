package com.example.bankcards.dto.transaction;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequestDTO {
    @NotNull(message = "From-card id must not be null")
    @Min(value = 0, message = "From-card id must not be negative")
    private Long fromCardId;
    @NotNull(message = "To-card id must not be null")
    @Min(value = 0, message = "To-card id must not be negative")
    private Long toCardId;
    @NotNull(message = "Transaction amount must not be null")
    @DecimalMin(value = "0.0", message = "Transaction amount must not be negative")
    private BigDecimal amount;
    @Size(max = 200, message = "Description must be shorter than 200 symbols")
    private String description;
}
