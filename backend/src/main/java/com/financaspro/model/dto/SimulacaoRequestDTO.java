package com.financaspro.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
public class SimulacaoRequestDTO {

    private String nome;

    @NotBlank(message = "Tipo de ativo é obrigatório (PREFIXADO, POS_CDI, IPCA_MAIS)")
    private String tipoAtivo;

    @NotNull(message = "Valor investido é obrigatório")
    @Min(value = 1, message = "Valor investido deve ser maior que 0")
    private BigDecimal valorInvestido;

    @NotNull(message = "Taxa ao ano (ou % do CDI) é obrigatória")
    private BigDecimal taxaAno;

    @NotNull(message = "Prazo em meses é obrigatório")
    @Min(value = 1, message = "Prazo em meses deve ser no mínimo 1")
    private Integer prazoMeses;

    private BigDecimal taxaCdiAno;
    private BigDecimal taxaIpcaAno;
    private Boolean isIsentoIr;
}
