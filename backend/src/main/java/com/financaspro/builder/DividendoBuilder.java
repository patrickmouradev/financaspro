package com.financaspro.builder;

import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class DividendoBuilder {

    private Long id;
    private Fii fii;
    private LocalDate dataPagamento;
    private BigDecimal valorPorCota;
    private BigDecimal quantidadeCotas;
    private BigDecimal valorTotal;
    private OffsetDateTime criadoEm;

    public static DividendoBuilder umDividendo() {
        return new DividendoBuilder();
    }

    public DividendoBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public DividendoBuilder comFii(Fii fii) {
        this.fii = fii;
        return this;
    }

    public DividendoBuilder comDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
        return this;
    }

    public DividendoBuilder comValorPorCota(BigDecimal valorPorCota) {
        this.valorPorCota = valorPorCota;
        return this;
    }

    public DividendoBuilder comQuantidadeCotas(BigDecimal quantidadeCotas) {
        this.quantidadeCotas = quantidadeCotas;
        return this;
    }

    public DividendoBuilder comValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
        return this;
    }

    public DividendoBuilder comCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
        return this;
    }

    public Dividendo build() {
        Dividendo div = new Dividendo();
        div.setId(this.id);
        div.setFii(this.fii);
        div.setDataPagamento(this.dataPagamento);
        div.setValorPorCota(this.valorPorCota);
        div.setQuantidadeCotas(this.quantidadeCotas);
        div.setValorTotal(this.valorTotal);
        div.setCriadoEm(this.criadoEm != null ? this.criadoEm : OffsetDateTime.now());
        return div;
    }
}
