package com.financaspro.builder;

import com.financaspro.model.dto.RegraCategoriaDTO;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.RegraCategoria;

public final class RegraCategoriaBuilder {

    private RegraCategoriaBuilder() {
        // Construtor privado
    }

    public static RegraCategoria criar(Categoria categoria, String palavraChave, Integer prioridade) {
        return RegraCategoria.builder()
                .categoria(categoria)
                .palavraChave(palavraChave != null ? palavraChave.toUpperCase().trim() : "")
                .prioridade(prioridade != null ? prioridade : 1)
                .build();
    }

    public static RegraCategoriaDTO paraDTO(RegraCategoria entity) {
        if (entity == null) {
            return null;
        }
        return RegraCategoriaDTO.builder()
                .id(entity.getId())
                .categoriaId(entity.getCategoria() != null ? entity.getCategoria().getId() : null)
                .categoriaNome(entity.getCategoria() != null ? entity.getCategoria().getNome() : null)
                .palavraChave(entity.getPalavraChave())
                .prioridade(entity.getPrioridade())
                .build();
    }
}
