package com.financaspro.service;

import com.financaspro.builder.UsuarioBuilder;
import com.financaspro.model.dto.LoginRequestDTO;
import com.financaspro.model.dto.TokenResponseDTO;
import com.financaspro.model.entity.Usuario;
import com.financaspro.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretKey secretKey;
    private final long expirationMs;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expirationMs) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public TokenResponseDTO autenticar(LoginRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário ou senha inválidos"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new RuntimeException("Usuário inativo");
        }

        if (!passwordEncoder.matches(request.getSenha(), usuario.getSenhaHash())) {
            throw new RuntimeException("Usuário ou senha inválidos");
        }

        String token = gerarToken(usuario);
        return UsuarioBuilder.criarTokenResponse(token, usuario);
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date validade = new Date(agora.getTime() + expirationMs);

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nome", usuario.getNome())
                .issuedAt(agora)
                .expiration(validade)
                .signWith(secretKey)
                .compact();
    }

    public String validarTokenEObterSubject(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    public TokenResponseDTO renovarToken(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new RuntimeException("Token num formato inválido");
        }
        String token = bearerToken.substring(7);
        String email = validarTokenEObterSubject(token);
        if (email == null) {
            throw new RuntimeException("Token expirado ou inválido");
        }
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!Boolean.TRUE.equals(usuario.getAtivo())) {
            throw new RuntimeException("Usuário inativo");
        }

        String novoToken = gerarToken(usuario);
        return UsuarioBuilder.criarTokenResponse(novoToken, usuario);
    }
}
