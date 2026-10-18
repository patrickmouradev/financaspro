package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CotacaoDetalheDTO {
    private String ticker;
    private BigDecimal precoAtual;
    private String provedor;
    private boolean emTempoReal;
    private Instant atualizadoEm;
    private String mensagem;
}
