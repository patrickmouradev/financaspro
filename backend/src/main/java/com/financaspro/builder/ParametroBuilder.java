package com.financaspro.builder;

import com.financaspro.model.dto.ParametroDTO;
import com.financaspro.model.entity.Parametro;

public final class ParametroBuilder {

    private ParametroBuilder() {
        // Construtor privado
    }

    public static Parametro criarParametro(String chave, String valor, String descricao, String tipo) {
        return Parametro.builder()
                .chave(chave)
                .valor(valor)
                .descricao(descricao)
                .tipo(tipo != null ? tipo : "STRING")
                .build();
    }

    public static ParametroDTO paraDTO(Parametro entity) {
        if (entity == null) {
            return null;
        }
        return ParametroDTO.builder()
                .id(entity.getId())
                .chave(entity.getChave())
                .valor("PASSWORD".equalsIgnoreCase(entity.getTipo()) ? "*****" : entity.getValor())
                .descricao(entity.getDescricao())
                .tipo(entity.getTipo())
                .atualizadoEm(entity.getAtualizadoEm())
                .build();
    }
}
