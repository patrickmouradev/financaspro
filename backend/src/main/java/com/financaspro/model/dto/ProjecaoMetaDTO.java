package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjecaoMetaDTO {

    private MetaDTO resumoMeta;
    private List<ProjecaoFiiItemDTO> projecoesPorFii;
    private BigDecimal aporteTotalEstimadoNecessario;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjecaoFiiItemDTO {
        private Long fiiId;
        private String ticker;
        private String nome;
        private BigDecimal cotasAtuais;
        private BigDecimal precoMedio;
        private BigDecimal precoAtual;
        private BigDecimal valorTotalInvestido;
        private BigDecimal valorAtualTotal;
        private BigDecimal dividendoMedioPorCota;
        private BigDecimal rendimentoMensalAtual;
        private BigDecimal cotasFaltantesParaMetaTotal;
        private BigDecimal investimentoEstimadoNecessario;
        private List<OperacaoCompraItemDTO> compras;

        // Métricas de Mercado & Classificação Financeira Inteligente
        private BigDecimal dividendYieldMensalAtual;  // % a.m. calculado sobre o preço real de mercado
        private BigDecimal dividendYieldAnualizado;    // % a.a. anualizado
        private BigDecimal custoParaUmRealRenda;       // R$ de aporte necessário para gerar R$ 1,00/mês
        private Integer rankingOportunidade;           // 1 = melhor opção de compra da carteira
        private String classificacaoCompra;            // "1º - MELHOR COMPRA", "2º - ÓTIMA OPÇÃO", etc.
        private String recomendacaoCompra;             // Justificativa baseada no yield atual
        private boolean cotacaoEmTempoReal;            // true se atualizada via B3 / Yahoo Finance
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OperacaoCompraItemDTO {
        private Long id;
        private LocalDate dataCompra;
        private BigDecimal quantidade;
        private BigDecimal precoUnitario;
        private BigDecimal taxasB3;
        private BigDecimal precoMedioAjustadoOperacao;
        private BigDecimal valorTotalPago;
        private String origemOuNota;
    }
}
