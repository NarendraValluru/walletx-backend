package com.example.walletxbackend.service.impl;

import com.example.walletxbackend.dto.request.LoginRequest;
import com.example.walletxbackend.dto.request.SignupRequest;
import com.example.walletxbackend.dto.response.AuthResponse;
import com.example.walletxbackend.entity.User;
import com.example.walletxbackend.entity.Role;
import com.example.walletxbackend.repository.UserRepository;
import com.example.walletxbackend.service.UserService;
import com.example.walletxbackend.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of {@link UserService} handling sign‑up and login.
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(SignupRequest request) {
        // Validate uniqueness
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        log.info("Login attempt for identifier: {}", request.usernameOrEmail());
        // Find by username or email
        Optional<User> optUser = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()));
        User user = optUser.orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        log.info("Password validation successful for username: {}", user.getUsername());
        String token = jwtUtil.generateToken(user);
        log.info("User logged in Successfully username: {}", user.getUsername());
        return new AuthResponse(token);
    }

    @Transactional
    @Override
    public void logout(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);

        log.info("User logged out successfully: {}", username);
    }
}
