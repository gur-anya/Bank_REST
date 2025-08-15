package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CardExpirationScheduler {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkCardExpiration() {
        LocalDate today = LocalDate.now();

        List<Card> expiredCards = cardRepository.findAllByStatusAndExpiryDateBefore(CardStatus.ACTIVE, today);

        if (expiredCards.isEmpty()) {
            return;
        }


        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
        }

        cardRepository.saveAll(expiredCards);
    }
}