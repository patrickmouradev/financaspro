package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
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
        private BigDecimal dividendoMedioPorCota;
        private BigDecimal rendimentoMensalAtual;
        private BigDecimal cotasFaltantesParaMetaTotal;
        private BigDecimal investimentoEstimadoNecessario;
    }
}
