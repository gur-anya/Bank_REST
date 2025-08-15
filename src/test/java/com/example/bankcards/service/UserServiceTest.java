package com.example.bankcards.service;


import com.example.bankcards.dto.auth.CreateUserRequestDTO;
import com.example.bankcards.dto.auth.CreateUserResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.NoUserActivenessUpdateException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.GetAllUsersMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private GetAllUsersMapper getAllUsersMapper;
    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setIsActive(true);
    }

    @Test
    void createUser_whenEmailIsUnique_shouldCreateUser() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test", "new@example.com", "pass");
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        CreateUserResponseDTO response = userService.createUser(request);

        assertNotNull(response);
        assertEquals("new@example.com", response.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailExists_shouldThrowException() {
        CreateUserRequestDTO request = new CreateUserRequestDTO("Test", "test@example.com", "pass");
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserNotFound_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    void blockUser_whenUserIsActive_shouldBlockUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.blockUser(1L);

        assertFalse(testUser.getIsActive());
    }

    @Test
    void blockUser_whenUserAlreadyBlocked_shouldThrowException() {
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(NoUserActivenessUpdateException.class, () -> userService.blockUser(1L));
    }

    @Test
    void unblockUser_whenUserIsBlocked_shouldUnblockUser() {
        testUser.setIsActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.unblockUser(1L);

        assertTrue(testUser.getIsActive());
    }

    @Test
    void unblockUser_whenUserAlreadyActive_shouldThrowException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        assertThrows(NoUserActivenessUpdateException.class, () -> userService.unblockUser(1L));
    }
}