package com.financaspro.service;

import com.financaspro.modulo.rendafixa.service.CalculadoraRendaFixaService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financaspro.model.dto.ComparacaoDTO;
import com.financaspro.model.dto.SimulacaoRequestDTO;
import com.financaspro.model.dto.SimulacaoResultadoDTO;
import com.financaspro.repository.SimulacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CalculadoraRendaFixaServiceTest {

    private IpcaService ipcaService;
    private SimulacaoRepository simulacaoRepository;
    private ObjectMapper objectMapper;
    private CalculadoraRendaFixaService calculadoraService;

    @BeforeEach
    void setUp() {
        ipcaService = Mockito.mock(IpcaService.class);
        simulacaoRepository = Mockito.mock(SimulacaoRepository.class);
        objectMapper = new ObjectMapper();

        when(ipcaService.obterUltimaTaxaOuPadrao(eq("CDI"), Mockito.any())).thenReturn(new BigDecimal("10.00"));
        when(ipcaService.obterUltimaTaxaOuPadrao(eq("IPCA"), Mockito.any())).thenReturn(new BigDecimal("4.00"));

        calculadoraService = new CalculadoraRendaFixaService(ipcaService, simulacaoRepository, objectMapper);
    }

    @Test
    void simular_Prefixado_IsentoIr_DeveCalcularSemRetencaoImposto() {
        SimulacaoRequestDTO req = SimulacaoRequestDTO.builder()
                .nome("LCI Prefixada")
                .tipoAtivo("PREFIXADO")
                .valorInvestido(new BigDecimal("10000.00"))
                .taxaAno(new BigDecimal("12.00"))
                .prazoMeses(12)
                .isIsentoIr(true)
                .build();

        SimulacaoResultadoDTO res = calculadoraService.simular(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("11200.00"), res.getValorBruto());
        assertEquals(0, new BigDecimal("0.00").compareTo(res.getAliquotaIr()));
        assertEquals(0, new BigDecimal("0.00").compareTo(res.getValorImpostoRenda()));
        assertEquals(new BigDecimal("11200.00"), res.getValorLiquido());
        assertEquals(new BigDecimal("1200.00"), res.getRendimentoLiquido());
    }

    @Test
    void simular_PosCdi_ComIr_DeveAplicarTabelaRegressiva() {
        SimulacaoRequestDTO req = SimulacaoRequestDTO.builder()
                .nome("CDB 100% CDI")
                .tipoAtivo("POS_CDI")
                .valorInvestido(new BigDecimal("10000.00"))
                .taxaAno(new BigDecimal("100.00")) // 100% do CDI (10% a.a.)
                .prazoMeses(12) // 360 dias -> aliquota IR 20.00%
                .isIsentoIr(false)
                .build();

        SimulacaoResultadoDTO res = calculadoraService.simular(req);

        assertNotNull(res);
        assertEquals(new BigDecimal("11000.00"), res.getValorBruto());
        assertEquals(new BigDecimal("20.00"), res.getAliquotaIr());
        assertEquals(new BigDecimal("200.00"), res.getValorImpostoRenda());
        assertEquals(new BigDecimal("10800.00"), res.getValorLiquido());
    }

    @Test
    void comparar_DuasOpcoes_DeveIdentificarAMelhorRendimento() {
        SimulacaoRequestDTO op1 = SimulacaoRequestDTO.builder()
                .nome("CDB 12% a.a.")
                .tipoAtivo("PREFIXADO")
                .valorInvestido(new BigDecimal("10000.00"))
                .taxaAno(new BigDecimal("12.00"))
                .prazoMeses(12)
                .isIsentoIr(true)
                .build();

        SimulacaoRequestDTO op2 = SimulacaoRequestDTO.builder()
                .nome("CDB 8% a.a.")
                .tipoAtivo("PREFIXADO")
                .valorInvestido(new BigDecimal("10000.00"))
                .taxaAno(new BigDecimal("8.00"))
                .prazoMeses(12)
                .isIsentoIr(true)
                .build();

        ComparacaoDTO comp = calculadoraService.comparar(op1, op2);

        assertNotNull(comp);
        assertEquals("CDB 12% a.a.", comp.getMelhorOpcao());
        assertEquals(new BigDecimal("400.00"), comp.getDiferencaValorLiquido());
    }
}
