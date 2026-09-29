package com.financaspro.repository;

import com.financaspro.model.entity.IndicadorEconomico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndicadorRepository extends JpaRepository<IndicadorEconomico, Long> {

    Optional<IndicadorEconomico> findByTipoAndAnoMes(String tipo, String anoMes);

    List<IndicadorEconomico> findByTipoOrderByAnoMesDesc(String tipo);

    Optional<IndicadorEconomico> findTop1ByTipoOrderByAnoMesDesc(String tipo);
}
