package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtivoDetectadoDTO {

    private String ticker;
    private String nome;
    private String tipo;          // FII, ACAO, RENDA_FIXA, OUTROS
    private String classe;        // Fundos Imobiliarios, ETF, CDB, etc.
    private String indexador;     // CDI, IPCA, etc.
    private boolean cadastrado;   // true se ja existe no banco
    private String status;        // CADASTRADO ou NOVO - SERA CADASTRADO
}
