package com.financaspro.repository;

import com.financaspro.model.entity.SimulacaoSalva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SimulacaoRepository extends JpaRepository<SimulacaoSalva, Long> {

    List<SimulacaoSalva> findAllByOrderByCriadoEmDesc();
}
