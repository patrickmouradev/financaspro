package com.financaspro.service;

import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.RegraCategoria;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.RegraCategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CategorizacaoServiceTest {

    private RegraCategoriaRepository regraCategoriaRepository;
    private CategoriaRepository categoriaRepository;
    private CategorizacaoService categorizacaoService;

    private Categoria categoriaAlimentacao;
    private Categoria categoriaSupermercado;
    private Categoria categoriaOutros;

    @BeforeEach
    void setUp() {
        regraCategoriaRepository = Mockito.mock(RegraCategoriaRepository.class);
        categoriaRepository = Mockito.mock(CategoriaRepository.class);
        categorizacaoService = new CategorizacaoService(regraCategoriaRepository, categoriaRepository);

        categoriaAlimentacao = Categoria.builder().id(1L).nome("Alimentação").tipo("DESPESA").build();
        categoriaSupermercado = Categoria.builder().id(2L).nome("Supermercado").tipo("DESPESA").build();
        categoriaOutros = Categoria.builder().id(99L).nome("Outros").tipo("DESPESA").build();

        RegraCategoria r1 = RegraCategoria.builder().id(1L).categoria(categoriaAlimentacao).palavraChave("RESTAURANTE").prioridade(1).build();
        RegraCategoria r2 = RegraCategoria.builder().id(2L).categoria(categoriaSupermercado).palavraChave("CARREFOUR").prioridade(1).build();

        when(regraCategoriaRepository.findAllOrderByPrioridadeEPalavraChaveLength()).thenReturn(List.of(r1, r2));
        when(categoriaRepository.findByNomeIgnoreCase("Outros")).thenReturn(Optional.of(categoriaOutros));
    }

    @Test
    void categorizarLancamento_ComMatchPalavraChave_DeveDefinirCategoriaEStatusAuto() {
        Lancamento l = Lancamento.builder()
                .descricao("Restaurante Garfo de Prata")
                .valor(new BigDecimal("50.00"))
                .dataLancamento(LocalDateTime.now())
                .build();

        categorizacaoService.categorizarLancamento(l);

        assertNotNull(l.getCategoria());
        assertEquals("Alimentação", l.getCategoria().getNome());
        assertEquals("AUTO", l.getStatusCategorizacao());
    }

    @Test
    void categorizarLancamento_SemMatch_DeveAtribuirOutrosEPendente() {
        Lancamento l = Lancamento.builder()
                .descricao("Loja Desconhecida XYZ")
                .valor(new BigDecimal("100.00"))
                .dataLancamento(LocalDateTime.now())
                .build();

        categorizacaoService.categorizarLancamento(l);

        assertNotNull(l.getCategoria());
        assertEquals("Outros", l.getCategoria().getNome());
        assertEquals("PENDENTE", l.getStatusCategorizacao());
    }
}
