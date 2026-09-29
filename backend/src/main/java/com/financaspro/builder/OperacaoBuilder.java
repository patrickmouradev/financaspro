package com.financaspro.builder;

import com.financaspro.model.entity.Ativo;
import com.financaspro.model.entity.Operacao;
import com.financaspro.model.enums.TipoOperacao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class OperacaoBuilder {

    private Long id;
    private Ativo ativo;
    private TipoOperacao tipo;
    private LocalDate dataOperacao;
    private BigDecimal quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal taxas;
    private String observacao;
    private OffsetDateTime criadoEm;

    public static OperacaoBuilder umaOperacao() {
        return new OperacaoBuilder();
    }

    public OperacaoBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public OperacaoBuilder comAtivo(Ativo ativo) {
        this.ativo = ativo;
        return this;
    }

    public OperacaoBuilder comTipo(TipoOperacao tipo) {
        this.tipo = tipo;
        return this;
    }

    public OperacaoBuilder comDataOperacao(LocalDate dataOperacao) {
        this.dataOperacao = dataOperacao;
        return this;
    }

    public OperacaoBuilder comQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
        return this;
    }

    public OperacaoBuilder comPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
        return this;
    }

    public OperacaoBuilder comTaxas(BigDecimal taxas) {
        this.taxas = taxas;
        return this;
    }

    public OperacaoBuilder comObservacao(String observacao) {
        this.observacao = observacao;
        return this;
    }

    public OperacaoBuilder comCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
        return this;
    }

    public Operacao build() {
        Operacao operacao = new Operacao();
        operacao.setId(this.id);
        operacao.setAtivo(this.ativo);
        operacao.setTipo(this.tipo);
        operacao.setDataOperacao(this.dataOperacao);
        operacao.setQuantidade(this.quantidade);
        operacao.setPrecoUnitario(this.precoUnitario);
        operacao.setTaxas(this.taxas != null ? this.taxas : BigDecimal.ZERO);
        operacao.setObservacao(this.observacao);
        operacao.setCriadoEm(this.criadoEm != null ? this.criadoEm : OffsetDateTime.now());
        return operacao;
    }
}
