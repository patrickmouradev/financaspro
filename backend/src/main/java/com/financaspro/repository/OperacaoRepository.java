package com.financaspro.repository;

import com.financaspro.model.entity.Operacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacaoRepository extends JpaRepository<Operacao, Long> {

    List<Operacao> findByAtivoIdOrderByDataOperacaoAsc(Long ativoId);

    List<Operacao> findByAtivoIdInOrderByDataOperacaoAsc(List<Long> ativoIds);

    List<Operacao> findAllByOrderByDataOperacaoDesc();

    List<Operacao> findByAtivoTickerOrderByDataOperacaoAsc(String ticker);
}
