package com.financaspro.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "indicador_economico", uniqueConstraints = {
        @UniqueConstraint(name = "uk_indicador_tipo_anomes", columnNames = {"tipo", "ano_mes"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IndicadorEconomico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String tipo; // IPCA, CDI, SELIC

    @Column(name = "ano_mes", nullable = false, length = 7)
    private String anoMes; // YYYY-MM

    @Column(name = "valor_percentual", nullable = false, precision = 10, scale = 4)
    private BigDecimal valorPercentual;

    @Column(nullable = false, length = 50)
    private String fonte; // BCB_SGS

    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.fonte == null) {
            this.fonte = "BCB_SGS";
        }
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
