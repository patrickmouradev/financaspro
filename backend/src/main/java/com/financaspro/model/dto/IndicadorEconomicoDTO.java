package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicadorEconomicoDTO {

    private Long id;
    private String tipo;
    private String anoMes;
    private BigDecimal valorPercentual;
    private String fonte;
    private OffsetDateTime criadoEm;
}
