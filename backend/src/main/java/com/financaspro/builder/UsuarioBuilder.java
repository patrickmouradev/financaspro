package com.financaspro.builder;

import com.financaspro.model.dto.TokenResponseDTO;
import com.financaspro.model.entity.Usuario;

public final class UsuarioBuilder {

    private UsuarioBuilder() {
        // Construtor privado
    }

    public static Usuario criarUsuario(String nome, String email, String senhaHash) {
        return Usuario.builder()
                .nome(nome)
                .email(email)
                .senhaHash(senhaHash)
                .ativo(true)
                .build();
    }

    public static TokenResponseDTO criarTokenResponse(String token, Usuario usuario) {
        return TokenResponseDTO.builder()
                .token(token)
                .tipo("Bearer")
                .nome(usuario != null ? usuario.getNome() : "")
                .email(usuario != null ? usuario.getEmail() : "")
                .build();
    }
}
