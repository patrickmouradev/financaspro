package com.financaspro.model.entity;

import com.financaspro.model.enums.TipoAtivo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "ativo")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String ticker;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAtivo tipo;

    @Column(name = "categoria_nome", length = 100)
    private String categoriaNome;

    @Column(length = 100)
    private String classe;

    @Column(length = 50)
    private String indexador;

    @Column(name = "taxa_adicional", precision = 10, scale = 4)
    private BigDecimal taxaAdicional;

    @Column(length = 100)
    private String setor;

    @Column(name = "data_aplicacao")
    private LocalDate dataAplicacao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(length = 100)
    private String liquidez;

    @Column(name = "porcentagem_taxa", length = 100)
    private String porcentagemTaxa;

    @Column(length = 150)
    private String emissor;

    @Column(name = "cnpj_emissor", length = 30)
    private String cnpjEmissor;

    @Column(length = 150)
    private String produto;

    @Column(name = "criado_em", updatable = false)
    private OffsetDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = OffsetDateTime.now();
        }
    }
}
