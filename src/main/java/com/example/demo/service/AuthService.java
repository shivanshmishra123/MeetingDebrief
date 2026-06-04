package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.dto.AuthResponse;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Business logic for user registration and login.
 *
 * REGISTRATION flow:
 *   1. Check that the email is not already taken → throw if duplicate
 *   2. BCrypt-hash the raw password
 *   3. Persist the new User row
 *   4. Generate + return a JWT
 *
 * LOGIN flow:
 *   1. Load the User by email → throw if not found
 *   2. Compare the raw password against the stored BCrypt hash
 *   3. Update last_login_at timestamp
 *   4. Generate + return a JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(AuthRequest request) {
        // Prevent duplicate email registrations
        if (userRepository.existsByEmail(request.email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }

        User user = new User();
        user.setName(request.name);
        user.setEmail(request.email);
        // IMPORTANT: always hash before storing — never persist raw passwords
        user.setPasswordHash(passwordEncoder.encode(request.password));
        user.setDesignation(request.designation != null ? request.designation : "");
        user.setActive(true);

        User savedUser = userRepository.save(user);
        System.out.println("✅ New user registered: " + savedUser.getEmail());

        String token = jwtService.generateToken(savedUser);
        return new AuthResponse(token, savedUser.getName(), savedUser.getEmail(), savedUser.getDesignation());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        // Load user — throw a generic error to not reveal whether the email exists
        User user = userRepository.findByEmail(request.email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password."));

        // Verify the supplied password against the BCrypt hash
        if (!passwordEncoder.matches(request.password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("This account has been deactivated.");
        }

        // Track last login time
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        System.out.println("✅ User logged in: " + user.getEmail());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getDesignation());
    }
}
