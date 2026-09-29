package com.financaspro.builder;

import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.ContaBancaria;
import com.financaspro.model.entity.Lancamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class LancamentoBuilder {

    private LancamentoBuilder() {
        // Construtor privado
    }

    public static Lancamento criar(
            ContaBancaria conta,
            Categoria categoria,
            LocalDateTime dataLancamento,
            String descricao,
            BigDecimal valor,
            String tipo,
            String origem,
            String codigoAutorizacao,
            Integer parcelaAtual,
            Integer totalParcelas,
            String statusCategorizacao,
            String observacao) {

        return Lancamento.builder()
                .contaBancaria(conta)
                .categoria(categoria)
                .dataLancamento(dataLancamento)
                .descricao(descricao)
                .valor(valor)
                .tipo(tipo)
                .origem(origem)
                .codigoAutorizacao(codigoAutorizacao)
                .parcelaAtual(parcelaAtual)
                .totalParcelas(totalParcelas)
                .statusCategorizacao(statusCategorizacao != null ? statusCategorizacao : "PENDENTE")
                .observacao(observacao)
                .build();
    }

    public static LancamentoDTO paraDTO(Lancamento entity) {
        if (entity == null) {
            return null;
        }
        return LancamentoDTO.builder()
                .id(entity.getId())
                .contaBancariaId(entity.getContaBancaria() != null ? entity.getContaBancaria().getId() : null)
                .contaBancariaNome(entity.getContaBancaria() != null ? entity.getContaBancaria().getNome() : null)
                .categoriaId(entity.getCategoria() != null ? entity.getCategoria().getId() : null)
                .categoriaNome(entity.getCategoria() != null ? entity.getCategoria().getNome() : "Pendente")
                .categoriaCor(entity.getCategoria() != null ? entity.getCategoria().getCor() : "#9CA3AF")
                .categoriaIcone(entity.getCategoria() != null ? entity.getCategoria().getIcone() : "HelpCircle")
                .dataLancamento(entity.getDataLancamento())
                .descricao(entity.getDescricao())
                .valor(entity.getValor())
                .tipo(entity.getTipo())
                .origem(entity.getOrigem())
                .codigoAutorizacao(entity.getCodigoAutorizacao())
                .parcelaAtual(entity.getParcelaAtual())
                .totalParcelas(entity.getTotalParcelas())
                .statusCategorizacao(entity.getStatusCategorizacao())
                .observacao(entity.getObservacao())
                .build();
    }
}
