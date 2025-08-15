package com.example.bankcards.dto.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetAllCardsResponseDTO {
    private Page<CardDTO> cards;
}
