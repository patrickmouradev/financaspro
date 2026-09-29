package com.financaspro.model.dto;

import com.financaspro.model.enums.TipoOperacao;
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
public class OperacaoDTO {

    private Long id;

    @NotNull(message = "ID do ativo é obrigatório")
    private Long ativoId;

    private String tickerAtivo;
    private String nomeAtivo;

    @NotNull(message = "Tipo de operação é obrigatório (COMPRA/VENDA)")
    private TipoOperacao tipo;

    @NotNull(message = "Data da operação é obrigatória")
    private LocalDate dataOperacao;

    @NotNull(message = "Quantidade é obrigatória")
    private BigDecimal quantidade;

    @NotNull(message = "Preço unitário é obrigatório")
    private BigDecimal precoUnitario;

    private BigDecimal taxas;
    private BigDecimal totalOperacao;
    private String observacao;
    private OffsetDateTime criadoEm;
}
