package com.financaspro.model.dto;

import com.financaspro.model.enums.TipoAtivo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private String classe;
    private String indexador;
    private BigDecimal quantidadeAtual;
    private BigDecimal precoMedio;
    private BigDecimal precoAtual;
    private BigDecimal valorTotalInvestido;
    private BigDecimal valorAtual;
    private BigDecimal lucroPrejuizo;
    private BigDecimal variacaoPercentual;
    private BigDecimal percentualCarteira;

    // Campos adicionais de Renda Fixa
    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private String liquidez;
    private String porcentagemTaxa;
    private String emissor;
    private String cnpjEmissor;
    private String produto;
    private Long diasAteVencimento;
    private String statusVencimento;
}
