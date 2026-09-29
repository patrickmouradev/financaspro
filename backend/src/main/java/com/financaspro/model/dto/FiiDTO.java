package com.financaspro.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FiiDTO {

    private Long id;

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    private String segmento;
    private BigDecimal quantidadeCotas;
    private BigDecimal precoMedio;
    private BigDecimal valorTotalInvestido;
    private OffsetDateTime criadoEm;
}
