package com.financaspro.model.entity;

import com.financaspro.model.enums.TipoOperacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "operacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Operacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ativo_id", nullable = false)
    private Ativo ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoOperacao tipo;

    @Column(name = "data_operacao", nullable = false)
    private LocalDate dataOperacao;

    @Column(nullable = false, precision = 15, scale = 6)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 15, scale = 4)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal taxas;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(length = 100)
    private String liquidez;

    @Column(name = "porcentagem_taxa", length = 100)
    private String porcentagemTaxa;

    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.taxas == null) {
            this.taxas = BigDecimal.ZERO;
        }
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
