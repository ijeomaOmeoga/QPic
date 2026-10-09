package com.qpic.auth.service;

import com.qpic.auth.domain.User;
import com.qpic.auth.repo.UserRepository;
import com.qpic.auth.security.JwtService;
import com.qpic.auth.web.AuthDtos.AuthResponse;
import com.qpic.auth.web.AuthDtos.LoginRequest;
import com.qpic.auth.web.AuthDtos.RegisterRequest;
import com.qpic.auth.web.AuthDtos.UserDto;
import com.qpic.common.error.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = normalize(req.email());
        if (users.existsByEmail(email)) {
            throw ApiException.conflict("Email is already registered");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setDisplayName(req.displayName().trim());
        users.save(user);
        return tokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmail(normalize(req.email()))
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> ApiException.unauthorized("Invalid email or password"));
        return tokens(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(String refreshToken) {
        UUID userId = jwt.parseUserId(refreshToken, "refresh");
        User user = users.findById(userId).orElseThrow(() -> ApiException.unauthorized("User no longer exists"));
        return tokens(user);
    }

    @Transactional(readOnly = true)
    public UserDto me(UUID userId) {
        return toDto(users.findById(userId).orElseThrow(() -> ApiException.notFound("User not found")));
    }

    private AuthResponse tokens(User user) {
        return new AuthResponse(jwt.accessToken(user), jwt.refreshToken(user), jwt.accessTtlSeconds(), toDto(user));
    }

    private static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getEmail(), u.getDisplayName(), u.getCreatedAt());
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
