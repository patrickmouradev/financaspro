package com.financaspro.repository;

import com.financaspro.model.entity.Parametro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametroRepository extends JpaRepository<Parametro, Long> {

    Optional<Parametro> findByChave(String chave);
}
