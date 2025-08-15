package com.example.bankcards.dto.user;

import com.example.bankcards.entity.enums.RoleEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    Long id;
    String name;
    String email;
    RoleEnum role;
    boolean isActive;
}
