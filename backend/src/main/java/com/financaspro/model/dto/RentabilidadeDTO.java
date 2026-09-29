package com.financaspro.model.dto;

import com.financaspro.model.enums.TipoAtivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentabilidadeDTO {

    private Long ativoId;
    private String ticker;
    private String nomeAtivo;
    private TipoAtivo tipoAtivo;
    private String setor;
    private BigDecimal quantidadeAtual;
    private BigDecimal precoMedio;
    private BigDecimal precoAtual;
    private BigDecimal valorTotalInvestido;
    private BigDecimal valorAtual;
    private BigDecimal lucroPrejuizo;
    private BigDecimal variacaoPercentual;
    private BigDecimal percentualCarteira;
}
