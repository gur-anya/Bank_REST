package com.example.bankcards.service;

import com.example.bankcards.dto.auth.CreateUserRequestDTO;
import com.example.bankcards.dto.auth.CreateUserResponseDTO;
import com.example.bankcards.dto.user.GetAllUsersResponseDTO;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.RoleEnum;
import com.example.bankcards.exception.EmailAlreadyExistsException;
import com.example.bankcards.exception.NoUserActivenessUpdateException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.mapper.GetAllUsersMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final GetAllUsersMapper getAllUsersMapper;

    public CreateUserResponseDTO createUser(CreateUserRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();
        user.setName(requestDTO.getName());
        user.setEmail(requestDTO.getEmail());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setRole(RoleEnum.USER);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        return new CreateUserResponseDTO(
            savedUser.getId(),
            savedUser.getName(),
            savedUser.getEmail()
        );
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        userRepository.deleteById(user.getId());
    }

    @Transactional
    public void blockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (!user.getIsActive()) {
            throw new NoUserActivenessUpdateException(userId, true);
        }
        user.setIsActive(false);
    }

    @Transactional
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        if (user.getIsActive()) {
            throw new NoUserActivenessUpdateException(userId, false);
        }
        user.setIsActive(true);
    }

    public GetAllUsersResponseDTO getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return getAllUsersMapper.toDTO(users);
    }
}
