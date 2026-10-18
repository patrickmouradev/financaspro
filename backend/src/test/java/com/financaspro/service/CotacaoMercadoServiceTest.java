package com.financaspro.service;

import com.financaspro.model.dto.CotacaoDetalheDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CotacaoMercadoServiceTest {

    private CotacaoMercadoService cotacaoMercadoService;

    @BeforeEach
    void setUp() {
        cotacaoMercadoService = new CotacaoMercadoService();
    }

    @Test
    void isTickerB3Valido_DeveValidarCorretamente() {
        assertTrue(cotacaoMercadoService.isTickerB3Valido("PETR4"));
        assertTrue(cotacaoMercadoService.isTickerB3Valido("VALE3"));
        assertTrue(cotacaoMercadoService.isTickerB3Valido("MXRF11"));
        assertTrue(cotacaoMercadoService.isTickerB3Valido("XPML11"));
        assertTrue(cotacaoMercadoService.isTickerB3Valido("BTLG11"));
        assertTrue(cotacaoMercadoService.isTickerB3Valido("MXRF11.SA"));

        assertFalse(cotacaoMercadoService.isTickerB3Valido("CDB_BTG_CDI"));
        assertFalse(cotacaoMercadoService.isTickerB3Valido("TESOURO_SELIC"));
        assertFalse(cotacaoMercadoService.isTickerB3Valido("CRA_JBS"));
        assertFalse(cotacaoMercadoService.isTickerB3Valido(null));
        assertFalse(cotacaoMercadoService.isTickerB3Valido(""));
    }

    @Test
    void obterCotacaoAtual_TickerInvalido_DeveRetornarNullSemErro() {
        BigDecimal preco = cotacaoMercadoService.obterCotacaoAtual("CDB_BTG_CDI");
        assertNull(preco);

        preco = cotacaoMercadoService.obterCotacaoAtual(null);
        assertNull(preco);
    }

    @Test
    void obterDetalhesCotacao_TickerB3Real_DeveObterPrecoDaInternet() {
        CotacaoDetalheDTO detalhe = cotacaoMercadoService.obterDetalhesCotacao("MXRF11");

        assertNotNull(detalhe, "Detalhe da cotação não deve ser nulo");
        assertNotNull(detalhe.getPrecoAtual(), "Preço obtido não deve ser nulo");
        assertTrue(detalhe.getPrecoAtual().compareTo(BigDecimal.ZERO) > 0, "Preço da cotação deve ser maior que zero");
        assertNotNull(detalhe.getProvedor(), "Provedor deve estar identificado");
        assertTrue(detalhe.isEmTempoReal(), "Deve marcar como em tempo real");

        // Segunda chamada deve vir do cache
        CotacaoDetalheDTO detalheCache = cotacaoMercadoService.obterDetalhesCotacao("MXRF11");
        assertNotNull(detalheCache);
        assertEquals(detalhe.getPrecoAtual(), detalheCache.getPrecoAtual());
        assertTrue(detalheCache.getProvedor().contains("Cache"), "Segunda chamada deve indicar Cache");
    }
}
