package com.example.bankcards.mapper;

import com.example.bankcards.dto.card.CardDTO;
import com.example.bankcards.entity.Card;
import com.example.bankcards.util.CardNumberUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target = "cardNumber", expression = "java(decryptAndMask(card.getCardNumber()))")
    @Mapping(target = "userId", source = "user.id")
    CardDTO toDTO(Card card);

    default String decryptAndMask(byte[] encryptedCardNumberBytes) {
        if (encryptedCardNumberBytes == null) {
            return null;
        }
        String decrypted = CardNumberUtil.decryptCardNumber(encryptedCardNumberBytes);
        return CardNumberUtil.maskCardNumber(decrypted);
    }
}