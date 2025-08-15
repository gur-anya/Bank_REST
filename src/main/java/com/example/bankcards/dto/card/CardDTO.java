package com.example.bankcards.dto.card;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String cardNumber;
    private String cardHolder;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private Long userId;
}
