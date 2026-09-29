package com.financaspro.repository;

import com.financaspro.model.entity.RegraCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegraCategoriaRepository extends JpaRepository<RegraCategoria, Long> {

    @Query("SELECT r FROM RegraCategoria r JOIN FETCH r.categoria ORDER BY r.prioridade ASC, LENGTH(r.palavraChave) DESC")
    List<RegraCategoria> findAllOrderByPrioridadeEPalavraChaveLength();

    List<RegraCategoria> findByCategoriaId(Long categoriaId);
}
