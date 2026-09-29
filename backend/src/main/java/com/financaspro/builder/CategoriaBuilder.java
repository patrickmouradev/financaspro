package com.financaspro.builder;

import com.financaspro.model.dto.CategoriaDTO;
import com.financaspro.model.entity.Categoria;

import java.util.Collections;

public final class CategoriaBuilder {

    private CategoriaBuilder() {
        // Construtor privado
    }

    public static Categoria criar(String nome, String icone, String cor, String tipo) {
        return Categoria.builder()
                .nome(nome)
                .icone(icone)
                .cor(cor)
                .tipo(tipo != null ? tipo : "DESPESA")
                .build();
    }

    public static CategoriaDTO paraDTO(Categoria entity) {
        if (entity == null) {
            return null;
        }
        return CategoriaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .icone(entity.getIcone())
                .cor(entity.getCor())
                .tipo(entity.getTipo())
                .regras(Collections.emptyList())
                .build();
    }
}
