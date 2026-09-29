package com.financaspro.repository;

import com.financaspro.model.entity.Lancamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LancamentoRepository extends JpaRepository<Lancamento, Long> {

    List<Lancamento> findByDataLancamentoBetweenOrderByDataLancamentoDesc(LocalDateTime inicio, LocalDateTime fim);

    List<Lancamento> findByContaBancariaIdAndDataLancamentoBetween(Long contaId, LocalDateTime inicio, LocalDateTime fim);

    Optional<Lancamento> findByCodigoAutorizacaoAndDataLancamentoAndValor(
            String codigoAutorizacao, LocalDateTime dataLancamento, BigDecimal valor);

    Optional<Lancamento> findByDescricaoAndDataLancamentoAndValor(
            String descricao, LocalDateTime dataLancamento, BigDecimal valor);

    @Query("SELECT l.categoria.id, l.categoria.nome, l.categoria.cor, SUM(l.valor) " +
           "FROM Lancamento l WHERE l.dataLancamento BETWEEN :inicio AND :fim AND l.valor > 0 " +
           "GROUP BY l.categoria.id, l.categoria.nome, l.categoria.cor ORDER BY SUM(l.valor) DESC")
    List<Object[]> sumByCategoriaAndPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
