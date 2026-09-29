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
@Table(name = "fii")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fii {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String ticker;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 100)
    private String segmento;

    @Column(name = "quantidade_cotas", nullable = false, precision = 15, scale = 6)
    private BigDecimal quantidadeCotas;

    @Column(name = "preco_medio", nullable = false, precision = 15, scale = 4)
    private BigDecimal precoMedio;

    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.quantidadeCotas == null) {
            this.quantidadeCotas = BigDecimal.ZERO;
        }
        if (this.precoMedio == null) {
            this.precoMedio = BigDecimal.ZERO;
        }
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
