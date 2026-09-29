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
public class SimulacaoResultadoDTO {

    private Long id;
    private String nome;
    private String tipoAtivo;
    private BigDecimal valorInvestido;
    private BigDecimal valorBruto;
    private BigDecimal aliquotaIr;
    private BigDecimal valorImpostoRenda;
    private BigDecimal valorLiquido;
    private BigDecimal rendimentoBruto;
    private BigDecimal rendimentoLiquido;
    private BigDecimal rentabilidadeLiquidaPercentual;
    private BigDecimal ganhoRealPercentual;
    private Integer prazoMeses;
    private BigDecimal taxaCdiUtilizada;
    private BigDecimal taxaIpcaUtilizada;
}
