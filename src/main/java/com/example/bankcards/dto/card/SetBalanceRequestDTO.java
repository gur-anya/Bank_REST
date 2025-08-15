package com.example.bankcards.dto.card;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetBalanceRequestDTO {

    @NotNull(message = "Balance must not be null")
    @DecimalMin(value = "0.0", message = "Balance must not be negative")
    private BigDecimal newBalance;
}