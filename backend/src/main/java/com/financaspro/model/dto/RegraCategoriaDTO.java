package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegraCategoriaDTO {

    private Long id;
    private Long categoriaId;
    private String categoriaNome;
    private String palavraChave;
    private Integer prioridade;
}
