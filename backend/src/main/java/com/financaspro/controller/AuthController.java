package com.financaspro.controller;

import com.financaspro.model.dto.LoginRequestDTO;
import com.financaspro.model.dto.TokenResponseDTO;
import com.financaspro.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        TokenResponseDTO response = authService.autenticar(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDTO> refresh(@org.springframework.web.bind.annotation.RequestHeader("Authorization") String bearerToken) {
        TokenResponseDTO response = authService.renovarToken(bearerToken);
        return ResponseEntity.ok(response);
    }
}
