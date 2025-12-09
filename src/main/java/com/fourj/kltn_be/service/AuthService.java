package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.AuthResponse;
import com.fourj.kltn_be.dto.LoginRequest;
import com.fourj.kltn_be.dto.RegisterRequest;
import com.fourj.kltn_be.dto.UserDTO;
import com.fourj.kltn_be.entity.User;
import com.fourj.kltn_be.repository.UserRepository;
import com.fourj.kltn_be.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPreferences(request.getPreferences());

        User saved = userRepository.save(user);
        String token = jwtUtil.generateToken(saved.getUserId(), saved.getUsername());

        return new AuthResponse(
                token,
                "Bearer",
                saved.getUserId(),
                saved.getUsername(),
                saved.getEmail()
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Try to find user by username or email
        User user = userRepository.findByUsername(request.getUsernameOrEmail())
                .or(() -> userRepository.findByEmail(request.getUsernameOrEmail()))
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());

        return new AuthResponse(
                token,
                "Bearer",
                user.getUserId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}

