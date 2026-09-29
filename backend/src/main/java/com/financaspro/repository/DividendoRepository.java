package com.financaspro.repository;

import com.financaspro.model.entity.Dividendo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendoRepository extends JpaRepository<Dividendo, Long> {

    List<Dividendo> findByFiiIdOrderByDataPagamentoDesc(Long fiiId);

    List<Dividendo> findByDataPagamentoBetweenOrderByDataPagamentoDesc(LocalDate inicio, LocalDate fim);

    List<Dividendo> findAllByOrderByDataPagamentoDesc();
}
