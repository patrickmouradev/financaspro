package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumoMensalDTO {

    private String anoMes;
    private BigDecimal totalGasto;
    private BigDecimal totalReceita;
    private BigDecimal saldoMes;
    private BigDecimal metaGastos;
    private BigDecimal percentualMetaAtingido;

    @Builder.Default
    private List<CategoriaResumoDTO> porCategoria = new ArrayList<>();
}
