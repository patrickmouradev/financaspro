package com.financaspro.repository;

import com.financaspro.model.entity.ContaBancaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContaBancariaRepository extends JpaRepository<ContaBancaria, Long> {

    List<ContaBancaria> findByAtivoTrue();

    Optional<ContaBancaria> findByFinalCartaoAndAtivoTrue(String finalCartao);

    Optional<ContaBancaria> findByAgenciaAndNumeroContaAndAtivoTrue(String agencia, String numeroConta);
}
