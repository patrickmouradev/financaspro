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
public class CarteiraDTO {

    private BigDecimal valorTotalInvestido;
    private BigDecimal valorTotalAtual;
    private BigDecimal lucroPrejuizoTotal;
    private BigDecimal variacaoPercentualTotal;
    private List<RentabilidadeDTO> itens;
}
