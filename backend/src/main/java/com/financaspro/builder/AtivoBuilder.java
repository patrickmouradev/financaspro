package com.financaspro.builder;

import com.financaspro.model.entity.Ativo;
import com.financaspro.model.enums.TipoAtivo;

import java.time.OffsetDateTime;

public class AtivoBuilder {

    private Long id;
    private String ticker;
    private String nome;
    private TipoAtivo tipo;
    private String setor;
    private OffsetDateTime criadoEm;

    public static AtivoBuilder umAtivo() {
        return new AtivoBuilder();
    }

    public AtivoBuilder comId(Long id) {
        this.id = id;
        return this;
    }

    public AtivoBuilder comTicker(String ticker) {
        this.ticker = ticker;
        return this;
    }

    public AtivoBuilder comNome(String nome) {
        this.nome = nome;
        return this;
    }

    public AtivoBuilder comTipo(TipoAtivo tipo) {
        this.tipo = tipo;
        return this;
    }

    public AtivoBuilder comSetor(String setor) {
        this.setor = setor;
        return this;
    }

    public AtivoBuilder comCriadoEm(OffsetDateTime criadoEm) {
        this.criadoEm = criadoEm;
        return this;
    }

    public Ativo build() {
        Ativo ativo = new Ativo();
        ativo.setId(this.id);
        ativo.setTicker(this.ticker);
        ativo.setNome(this.nome);
        ativo.setTipo(this.tipo);
        ativo.setSetor(this.setor);
        ativo.setCriadoEm(this.criadoEm != null ? this.criadoEm : OffsetDateTime.now());
        return ativo;
    }
}
