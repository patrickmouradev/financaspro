package com.financaspro.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaDTO {

    @NotNull(message = "Meta de renda mensal é obrigatória")
    private BigDecimal metaMensal;

    private BigDecimal mediaRendaMensalAtual;
    private BigDecimal percentualConcluido;
    private BigDecimal faltaParaMetaMensal;
}
