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
public class ComparacaoDTO {

    private SimulacaoResultadoDTO opcao1;
    private SimulacaoResultadoDTO opcao2;
    private BigDecimal diferencaValorLiquido;
    private String melhorOpcao;
}
