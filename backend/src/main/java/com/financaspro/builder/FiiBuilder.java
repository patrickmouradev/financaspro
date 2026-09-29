package com.financaspro.builder;

import com.financaspro.model.entity.Fii;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FiiBuilder {

    private Long id;
    private String ticker;
    private String nome;
    private String segmento;
    private BigDecimal quantidadeCotas;
    private BigDecimal precoMedio;
    private OffsetDateTime criadoEm;

    public static FiiBuilder umFii() {
        return new FiiBuilder();
    }

    public FiiBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public FiiBuilder comTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public FiiBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public FiiBuilder comSegmento(String segmento) {
        this.segmento = segmento;
        return this;
    }

    public FiiBuilder comQuantidadeCotas(BigDecimal quantidadeCotas) {
        this.quantidadeCotas = quantidadeCotas;
        return this;
    }

    public FiiBuilder comPrecoMedio(BigDecimal precoMedio) {
        this.precoMedio = precoMedio;
        return this;
    }

    public FiiBuilder comCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
        return this;
    }

    public Fii build() {
        Fii fii = new Fii();
        fii.setId(this.id);
        fii.setTicker(this.ticker);
        fii.setNome(this.nome);
        fii.setSegmento(this.segmento);
        fii.setQuantidadeCotas(this.quantidadeCotas != null ? this.quantidadeCotas : BigDecimal.ZERO);
        fii.setPrecoMedio(this.precoMedio != null ? this.precoMedio : BigDecimal.ZERO);
        fii.setCriadoEm(this.criadoEm != null ? this.criadoEm : OffsetDateTime.now());
        return fii;
    }
}
