package com.example.bankcards.mapper;


import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toModel(UserDTO userDTO);

    UserDTO toDTO(User user);
}
