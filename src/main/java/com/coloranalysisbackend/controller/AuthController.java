package com.coloranalysisbackend.controller;

import com.coloranalysisbackend.service.AuthService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            String id = authService.register(req.getUsername(), req.getPassword());
            return ResponseEntity.ok(new IdResponse(id));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.getUsername(), req.getPassword());
            return ResponseEntity.ok(new TokenResponse(token));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(401).body("认证失败");
        }
    }

    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    public static class TokenResponse {
        private final String token;
    }

    @Data
    public static class IdResponse {
        private final String id;
    }
}