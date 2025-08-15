package com.example.bankcards.mapper;

import com.example.bankcards.dto.user.GetAllUsersResponseDTO;
import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.entity.User;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;


@Mapper(componentModel = "spring", uses = {UserMapper.class})
public abstract class GetAllUsersMapper {
    @Autowired
    protected UserMapper userMapper;

    public GetAllUsersResponseDTO toDTO(Page<User> users) {
        if (users == null) {
            return null;
        }

        Page<UserDTO> userDTOs = users.map(userMapper::toDTO);
        return new GetAllUsersResponseDTO(userDTOs);
    }
}