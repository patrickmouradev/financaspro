package com.financaspro.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "simulacao_salva")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulacaoSalva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(name = "tipo_ativo", nullable = false, length = 30)
    private String tipoAtivo; // PREFIXADO, POS_CDI, IPCA_MAIS

    @Column(name = "valor_investido", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorInvestido;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal taxa;

    @Column(length = 20)
    private String indexador;

    @Column(name = "resultado_json", nullable = false, columnDefinition = "TEXT")
    private String resultadoJson;

    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
