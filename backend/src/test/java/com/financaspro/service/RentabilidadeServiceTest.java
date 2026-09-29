package com.financaspro.service;

import com.financaspro.builder.AtivoBuilder;
import com.financaspro.builder.OperacaoBuilder;
import com.financaspro.model.dto.RentabilidadeDTO;
import com.financaspro.model.entity.Ativo;
import com.financaspro.model.entity.Operacao;
import com.financaspro.model.enums.TipoAtivo;
import com.financaspro.model.enums.TipoOperacao;
import com.financaspro.repository.AtivoRepository;
import com.financaspro.repository.OperacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RentabilidadeServiceTest {

    private AtivoRepository ativoRepository;
    private OperacaoRepository operacaoRepository;
    private RentabilidadeService rentabilidadeService;

    @BeforeEach
    void setUp() {
        ativoRepository = Mockito.mock(AtivoRepository.class);
        operacaoRepository = Mockito.mock(OperacaoRepository.class);
        RestTemplateBuilder restTemplateBuilder = Mockito.mock(RestTemplateBuilder.class);
        when(restTemplateBuilder.setConnectTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(Mockito.mock(org.springframework.web.client.RestTemplate.class));

        rentabilidadeService = new RentabilidadeService(ativoRepository, operacaoRepository, restTemplateBuilder);
    }

    @Test
    void calcularRentabilidadeAtivo_ComComprasEEscalonamento_DeveCalcularPrecoMedioCorreto() {
        Ativo ativo = AtivoBuilder.umAtivo()
                .comId(1L)
                .comTicker("PETR4")
                .comNome("Petrobras PN")
                .comTipo(TipoAtivo.ACAO)
                .build();

        Operacao op1 = OperacaoBuilder.umaOperacao()
                .comId(101L)
                .comAtivo(ativo)
                .comTipo(TipoOperacao.COMPRA)
                .comDataOperacao(LocalDate.of(2026, 1, 10))
                .comQuantidade(new BigDecimal("10.00"))
                .comPrecoUnitario(new BigDecimal("30.00"))
                .comTaxas(BigDecimal.ZERO)
                .build();

        Operacao op2 = OperacaoBuilder.umaOperacao()
                .comId(102L)
                .comAtivo(ativo)
                .comTipo(TipoOperacao.COMPRA)
                .comDataOperacao(LocalDate.of(2026, 2, 10))
                .comQuantidade(new BigDecimal("10.00"))
                .comPrecoUnitario(new BigDecimal("40.00"))
                .comTaxas(new BigDecimal("10.00"))
                .build();

        // Total investido: 300 + 410 = 710. Qtd total: 20. Preço médio: 710 / 20 = 35.50

        when(operacaoRepository.findByAtivoIdOrderByDataOperacaoAsc(1L)).thenReturn(List.of(op1, op2));

        RentabilidadeDTO rentabilidade = rentabilidadeService.calcularRentabilidadeAtivo(ativo);

        assertNotNull(rentabilidade);
        assertEquals(new BigDecimal("20.00"), rentabilidade.getQuantidadeAtual());
        assertEquals(new BigDecimal("35.50"), rentabilidade.getPrecoMedio());
        assertEquals(new BigDecimal("710.00"), rentabilidade.getValorTotalInvestido());
    }

    @Test
    void calcularRentabilidadeAtivo_ComVendaParcial_DeveManterPrecoMedio() {
        Ativo ativo = AtivoBuilder.umAtivo()
                .comId(1L)
                .comTicker("VALE3")
                .comNome("Vale ON")
                .comTipo(TipoAtivo.ACAO)
                .build();

        Operacao opCompra = OperacaoBuilder.umaOperacao()
                .comId(1L)
                .comAtivo(ativo)
                .comTipo(TipoOperacao.COMPRA)
                .comDataOperacao(LocalDate.of(2026, 1, 10))
                .comQuantidade(new BigDecimal("100.00"))
                .comPrecoUnitario(new BigDecimal("60.00"))
                .comTaxas(BigDecimal.ZERO)
                .build();

        Operacao opVenda = OperacaoBuilder.umaOperacao()
                .comId(2L)
                .comAtivo(ativo)
                .comTipo(TipoOperacao.VENDA)
                .comDataOperacao(LocalDate.of(2026, 2, 10))
                .comQuantidade(new BigDecimal("40.00"))
                .comPrecoUnitario(new BigDecimal("70.00"))
                .comTaxas(BigDecimal.ZERO)
                .build();

        when(operacaoRepository.findByAtivoIdOrderByDataOperacaoAsc(1L)).thenReturn(List.of(opCompra, opVenda));

        RentabilidadeDTO rentabilidade = rentabilidadeService.calcularRentabilidadeAtivo(ativo);

        assertNotNull(rentabilidade);
        assertEquals(new BigDecimal("60.00"), rentabilidade.getQuantidadeAtual());
        assertEquals(new BigDecimal("60.00"), rentabilidade.getPrecoMedio());
        assertEquals(new BigDecimal("3600.00"), rentabilidade.getValorTotalInvestido());
    }
}
