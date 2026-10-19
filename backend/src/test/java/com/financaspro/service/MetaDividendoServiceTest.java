package com.financaspro.service;

import com.financaspro.builder.DividendoBuilder;
import com.financaspro.builder.FiiBuilder;
import com.financaspro.model.dto.ProjecaoMetaDTO;
import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.repository.OperacaoRepository;
import com.financaspro.modulo.fundos.service.FiiService;
import com.financaspro.modulo.fundos.service.MetaDividendoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MetaDividendoServiceTest {

    private FiiRepository fiiRepository;
    private DividendoRepository dividendoRepository;
    private FiiService fiiService;
    private LancamentoRepository lancamentoRepository;
    private OperacaoRepository operacaoRepository;
    private CotacaoMercadoService cotacaoMercadoService;
    private MetaDividendoService metaDividendoService;

    @BeforeEach
    void setUp() {
        fiiRepository = Mockito.mock(FiiRepository.class);
        dividendoRepository = Mockito.mock(DividendoRepository.class);
        fiiService = Mockito.mock(FiiService.class);
        lancamentoRepository = Mockito.mock(LancamentoRepository.class);
        operacaoRepository = Mockito.mock(OperacaoRepository.class);
        cotacaoMercadoService = Mockito.mock(CotacaoMercadoService.class);
        metaDividendoService = new MetaDividendoService(fiiRepository, dividendoRepository, lancamentoRepository, operacaoRepository, fiiService, cotacaoMercadoService);
    }

    @Test
    void calcularProgressoEMeta_DeveCalcularMetaEPercentualConcluido() {
        Fii fii1 = FiiBuilder.umFii()
                .comId(1L)
                .comTicker("HGLG11")
                .comNome("CGHG Logística")
                .comQuantidadeCotas(new BigDecimal("100.00"))
                .comPrecoMedio(new BigDecimal("160.00"))
                .build();

        Dividendo d1 = DividendoBuilder.umDividendo()
                .comId(10L)
                .comFii(fii1)
                .comDataPagamento(LocalDate.now().minusDays(15))
                .comValorPorCota(new BigDecimal("1.10"))
                .comQuantidadeCotas(new BigDecimal("100.00"))
                .comValorTotal(new BigDecimal("110.00"))
                .build();

        when(fiiRepository.findAll()).thenReturn(List.of(fii1));
        when(dividendoRepository.findByFiiIdOrderByDataPagamentoDesc(eq(1L))).thenReturn(List.of(d1));

        // Meta: R$ 1.000,00/mês. Atual: 100 * 1.10 = 110.00. Concluído: (110 / 1000) * 100 = 11.00%. Falta: 890.00.
        ProjecaoMetaDTO projecao = metaDividendoService.calcularProgressoEMeta(new BigDecimal("1000.00"));

        assertNotNull(projecao);
        assertEquals(new BigDecimal("1000.00"), projecao.getResumoMeta().getMetaMensal());
        assertEquals(new BigDecimal("110.00"), projecao.getResumoMeta().getMediaRendaMensalAtual());
        assertEquals(new BigDecimal("11.00"), projecao.getResumoMeta().getPercentualConcluido());
        assertEquals(new BigDecimal("890.00"), projecao.getResumoMeta().getFaltaParaMetaMensal());
    }

    @Test
    void calcularProgressoEMeta_DeveCalcularAporteEstimadoIndividualParaAtingirMetaTotal() {
        // Cenário do usuário:
        // MXRF11 a R$ 9,34, dividendo R$ 0,08 por cota
        // Meta R$ 1.000,00, renda atual R$ 33,94 -> falta R$ 966,06
        // Cotas faltantes = 966.06 / 0.08 = 12075.75 -> 12076 cotas (CEILING)
        // Aporte estimado = 12076 * 9.34 = R$ 112.789,84
        Fii mxrf11 = FiiBuilder.umFii()
                .comId(2L)
                .comTicker("MXRF11")
                .comNome("Maxi Renda")
                .comQuantidadeCotas(new BigDecimal("424.25"))
                .comPrecoMedio(new BigDecimal("10.00"))
                .build();

        Dividendo dMxrf = DividendoBuilder.umDividendo()
                .comId(20L)
                .comFii(mxrf11)
                .comDataPagamento(LocalDate.now().minusDays(10))
                .comValorPorCota(new BigDecimal("0.08"))
                .comQuantidadeCotas(new BigDecimal("424.25"))
                .comValorTotal(new BigDecimal("33.94"))
                .build();

        when(fiiRepository.findAll()).thenReturn(List.of(mxrf11));
        when(dividendoRepository.findByFiiIdOrderByDataPagamentoDesc(eq(2L))).thenReturn(List.of(dMxrf));
        when(cotacaoMercadoService.obterCotacaoAtual("MXRF11")).thenReturn(new BigDecimal("9.34"));

        ProjecaoMetaDTO projecao = metaDividendoService.calcularProgressoEMeta(new BigDecimal("1000.00"));

        assertNotNull(projecao);
        assertEquals(new BigDecimal("33.94"), projecao.getResumoMeta().getMediaRendaMensalAtual());
        assertEquals(new BigDecimal("966.06"), projecao.getResumoMeta().getFaltaParaMetaMensal());

        ProjecaoMetaDTO.ProjecaoFiiItemDTO item = projecao.getProjecoesPorFii().get(0);
        assertEquals("MXRF11", item.getTicker());
        assertEquals(new BigDecimal("9.34"), item.getPrecoAtual());
        assertEquals(new BigDecimal("0.0800"), item.getDividendoMedioPorCota());
        assertEquals(new BigDecimal("12076"), item.getCotasFaltantesParaMetaTotal());
        assertEquals(new BigDecimal("112789.84"), item.getInvestimentoEstimadoNecessario());
        assertEquals(new BigDecimal("112789.84"), projecao.getAporteTotalEstimadoNecessario());
    }

}
