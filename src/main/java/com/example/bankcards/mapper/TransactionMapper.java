package com.example.bankcards.mapper;

import com.example.bankcards.dto.transaction.TransactionDTO;
import com.example.bankcards.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    TransactionDTO toDTO(Transaction transaction);
}
