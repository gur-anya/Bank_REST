package com.example.bankcards.controller;


import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.user.GetAllUsersResponseDTO;
import com.example.bankcards.dto.user.UserDTO;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.security.JWTCore;
import com.example.bankcards.security.TokenFilter;
import com.example.bankcards.security.UserDetailsServiceImpl;
import com.example.bankcards.service.TokenService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {
    UserDetailsServiceAutoConfiguration.class,
    SecurityAutoConfiguration.class
})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    private JWTCore jwtCore;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private TokenService tokenService;
    @MockBean
    private TokenFilter tokenFilter;

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_whenAdmin_shouldReturnUserPage() throws Exception {
        UserDTO user = new UserDTO(1L, "Test User", "test@user.com", RoleEnum.USER, true);
        Pageable pageable = PageRequest.of(0, 10);
        GetAllUsersResponseDTO responseDTO = new GetAllUsersResponseDTO(new PageImpl<>(List.of(user), pageable, 1));

        doReturn(responseDTO).when(userService).getAllUsers(any(Pageable.class));

        mockMvc.perform(get("/api/v1/users?page=0&size=10"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.users.content[0].id").value(1L))
            .andExpect(jsonPath("$.users.content[0].name").value("Test User"))
            .andExpect(jsonPath("$.users.totalElements").value(1));

        verify(userService).getAllUsers(any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenAdminAndUserExists_shouldReturnNoContent() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
            .andExpect(status().isNoContent());

        verify(userService).deleteUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 99L;
        doThrow(new UserNotFoundException()).when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/v1/users/{userId}", userId))
            .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_whenAdminAndUserExists_shouldReturnOk() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).blockUser(userId);

        mockMvc.perform(patch("/api/v1/users/block/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User " + userId + " blocked successfully."));

        verify(userService).blockUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockUser_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 99L;
        doThrow(new UserNotFoundException()).when(userService).blockUser(userId);

        mockMvc.perform(patch("/api/v1/users/block/{userId}", userId))
            .andExpect(status().isNotFound());
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    void unblockUser_whenAdminAndUserExists_shouldReturnOk() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).unblockUser(userId);

        mockMvc.perform(patch("/api/v1/users/unblock/{userId}", userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("User " + userId + " unblocked successfully."));

        verify(userService).unblockUser(userId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void unblockUser_whenAdminAndUserNotFound_shouldReturnNotFound() throws Exception {
        Long userId = 99L;
        doThrow(new UserNotFoundException()).when(userService).unblockUser(userId);

        mockMvc.perform(patch("/api/v1/users/unblock/{userId}", userId))
            .andExpect(status().isNotFound());
    }
}