package com.financaspro.repository;

import com.financaspro.model.entity.Ativo;
import com.financaspro.model.enums.TipoAtivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AtivoRepository extends JpaRepository<Ativo, Long> {

    Optional<Ativo> findByTicker(String ticker);

    List<Ativo> findByTipo(TipoAtivo tipo);

    boolean existsByTicker(String ticker);
}
