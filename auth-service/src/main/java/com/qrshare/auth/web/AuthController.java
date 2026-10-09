package com.qpic.auth.web;

import com.qpic.auth.service.AuthService;
import com.qpic.auth.web.AuthDtos.AuthResponse;
import com.qpic.auth.web.AuthDtos.LoginRequest;
import com.qpic.auth.web.AuthDtos.RefreshRequest;
import com.qpic.auth.web.AuthDtos.RegisterRequest;
import com.qpic.auth.web.AuthDtos.UserDto;
import com.qpic.common.web.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService service;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return service.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return service.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return service.refresh(req.refreshToken());
    }

    @GetMapping("/me")
    public UserDto me(@CurrentUser UUID userId) {
        return service.me(userId);
    }
}
