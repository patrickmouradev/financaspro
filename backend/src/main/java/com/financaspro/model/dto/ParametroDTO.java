package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametroDTO {

    private Long id;
    private String chave;
    private String valor;
    private String descricao;
    private String tipo;
    private OffsetDateTime atualizadoEm;
}
