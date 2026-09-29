package com.financaspro.builder;

import com.financaspro.model.entity.SimulacaoSalva;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class SimulacaoBuilder {

    private Long id;
    private String nome;
    private String tipoAtivo;
    private BigDecimal valorInvestido;
    private BigDecimal taxa;
    private String indexador;
    private String resultadoJson;
    private OffsetDateTime criadoEm;

    public static SimulacaoBuilder umaSimulacao() {
        return new SimulacaoBuilder();
    }

    public SimulacaoBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public SimulacaoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public SimulacaoBuilder comTipoAtivo(String tipoAtivo) {
        this.tipoAtivo = tipoAtivo;
        return this;
    }

    public SimulacaoBuilder comValorInvestido(BigDecimal valorInvestido) {
        this.valorInvestido = valorInvestido;
        return this;
    }

    public SimulacaoBuilder comTaxa(BigDecimal taxa) {
        this.taxa = taxa;
        return this;
    }

    public SimulacaoBuilder comIndexador(String indexador) {
        this.indexador = indexador;
        return this;
    }

    public SimulacaoBuilder comResultadoJson(String resultadoJson) {
        this.resultadoJson = resultadoJson;
        return this;
    }

    public SimulacaoBuilder comCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
        return this;
    }

    public SimulacaoSalva build() {
        SimulacaoSalva sim = new SimulacaoSalva();
        sim.setId(this.id);
        sim.setNome(this.nome);
        sim.setTipoAtivo(this.tipoAtivo);
        sim.setValorInvestido(this.valorInvestido);
        sim.setTaxa(this.taxa);
        sim.setIndexador(this.indexador);
        sim.setResultadoJson(this.resultadoJson);
        sim.setCriadoEm(this.criadoEm != null ? this.criadoEm : OffsetDateTime.now());
        return sim;
    }
}
