package com.financaspro.repository;

import com.financaspro.model.entity.Fii;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FiiRepository extends JpaRepository<Fii, Long> {

    Optional<Fii> findByTicker(String ticker);

    boolean existsByTicker(String ticker);
}
