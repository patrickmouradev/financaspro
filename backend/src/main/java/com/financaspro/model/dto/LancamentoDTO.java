package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancamentoDTO {

    private Long id;
    private Long contaBancariaId;
    private String contaBancariaNome;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCor;
    private String categoriaIcone;
    private LocalDateTime dataLancamento;
    private String descricao;
    private BigDecimal valor;
    private String tipo;
    private String origem;
    private String codigoAutorizacao;
    private Integer parcelaAtual;
    private Integer totalParcelas;
    private String statusCategorizacao;
    private String observacao;

    // Detalhes Renda Fixa para Preview / Edição / Confirmação
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private String liquidez;
    private String indice;
    private String taxa;
    private String produto;
    private String emissor;
    private String cnpjEmissor;
}
