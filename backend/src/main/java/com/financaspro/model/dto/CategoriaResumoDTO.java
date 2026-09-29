package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResumoDTO {

    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCor;
    private BigDecimal valorTotal;
    private BigDecimal percentualDoTotal;
}
