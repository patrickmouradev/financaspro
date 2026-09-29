package com.financaspro.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DividendoDTO {

    private Long id;

    @NotNull(message = "ID do FII é obrigatório")
    private Long fiiId;

    private String tickerFii;
    private String nomeFii;

    @NotNull(message = "Data de pagamento é obrigatória")
    private LocalDate dataPagamento;

    @NotNull(message = "Valor por cota é obrigatório")
    private BigDecimal valorPorCota;

    private BigDecimal quantidadeCotas;
    private BigDecimal valorTotal;
    private OffsetDateTime criadoEm;
}
