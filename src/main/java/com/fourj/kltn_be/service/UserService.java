package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.PageResponse;
import com.fourj.kltn_be.dto.UserDTO;
import com.fourj.kltn_be.entity.User;
import com.fourj.kltn_be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        List<UserDTO> content = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public Optional<UserDTO> getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userDTO.getEmail() != null && userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPreferences(userDTO.getPreferences());
        
        User saved = userRepository.save(user);
        return convertToDTO(saved);
    }

    @Transactional
    public Optional<UserDTO> updateUser(Long userId, UserDTO userDTO) {
        return userRepository.findByUserId(userId)
                .map(existing -> {
                    if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existing.getEmail())) {
                        if (userRepository.existsByEmail(userDTO.getEmail())) {
                            throw new RuntimeException("Email already exists");
                        }
                        existing.setEmail(userDTO.getEmail());
                    }
                    if (userDTO.getPreferences() != null) {
                        existing.setPreferences(userDTO.getPreferences());
                    }
                    User saved = userRepository.save(existing);
                    return convertToDTO(saved);
                });
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPreferences(user.getPreferences());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }
}

