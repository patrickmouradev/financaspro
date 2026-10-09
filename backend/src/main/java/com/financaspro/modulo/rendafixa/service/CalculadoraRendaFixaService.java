package com.financaspro.modulo.rendafixa.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financaspro.builder.SimulacaoBuilder;
import com.financaspro.model.dto.ComparacaoDTO;
import com.financaspro.model.dto.SimulacaoRequestDTO;
import com.financaspro.model.dto.SimulacaoResultadoDTO;
import com.financaspro.model.entity.SimulacaoSalva;
import com.financaspro.repository.SimulacaoRepository;
import com.financaspro.service.IpcaService;
import com.financaspro.utils.MoedaUtils;
import com.financaspro.utils.PercentualUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class CalculadoraRendaFixaService {

    private final IpcaService ipcaService;
    private final SimulacaoRepository simulacaoRepository;
    private final ObjectMapper objectMapper;

    public CalculadoraRendaFixaService(IpcaService ipcaService,
                                       SimulacaoRepository simulacaoRepository,
                                       ObjectMapper objectMapper) {
        this.ipcaService = ipcaService;
        this.simulacaoRepository = simulacaoRepository;
        this.objectMapper = objectMapper;
    }

    public SimulacaoResultadoDTO simular(SimulacaoRequestDTO req) {
        BigDecimal valorInvestido = req.getValorInvestido();
        BigDecimal taxaAno = req.getTaxaAno();
        int prazoMeses = req.getPrazoMeses();
        boolean isIsento = Boolean.TRUE.equals(req.getIsIsentoIr());

        BigDecimal cdiAno = req.getTaxaCdiAno() != null ? req.getTaxaCdiAno() : ipcaService.obterUltimaTaxaOuPadrao("CDI", new BigDecimal("10.50"));
        BigDecimal ipcaAno = req.getTaxaIpcaAno() != null ? req.getTaxaIpcaAno() : ipcaService.obterUltimaTaxaOuPadrao("IPCA", new BigDecimal("4.50"));

        double anos = prazoMeses / 12.0;
        double valorInvestidoDbl = valorInvestido.doubleValue();
        double valorBrutoDbl = 0.0;

        String tipoUpper = req.getTipoAtivo().trim().toUpperCase();

        if ("PREFIXADO".equals(tipoUpper)) {
            double i = taxaAno.doubleValue() / 100.0;
            valorBrutoDbl = valorInvestidoDbl * Math.pow(1.0 + i, anos);
        } else if ("POS_CDI".equals(tipoUpper)) {
            double cdiDecimal = cdiAno.doubleValue() / 100.0;
            double pctCdi = taxaAno.doubleValue() / 100.0;
            double iEfetiva = cdiDecimal * pctCdi;
            valorBrutoDbl = valorInvestidoDbl * Math.pow(1.0 + iEfetiva, anos);
        } else if ("IPCA_MAIS".equals(tipoUpper)) {
            double ipcaDecimal = ipcaAno.doubleValue() / 100.0;
            double taxaFixaDecimal = taxaAno.doubleValue() / 100.0;
            double iComposta = (1.0 + ipcaDecimal) * (1.0 + taxaFixaDecimal) - 1.0;
            valorBrutoDbl = valorInvestidoDbl * Math.pow(1.0 + iComposta, anos);
        } else {
            throw new IllegalArgumentException("Tipo de ativo inválido. Use PREFIXADO, POS_CDI ou IPCA_MAIS.");
        }

        BigDecimal valorBruto = new BigDecimal(valorBrutoDbl).setScale(2, RoundingMode.HALF_UP);
        BigDecimal rendimentoBruto = valorBruto.subtract(valorInvestido);

        int prazoDias = prazoMeses * 30;
        BigDecimal aliquotaIr = BigDecimal.ZERO;

        if (!isIsento) {
            if (prazoDias <= 180) {
                aliquotaIr = new BigDecimal("22.50");
            } else if (prazoDias <= 360) {
                aliquotaIr = new BigDecimal("20.00");
            } else if (prazoDias <= 720) {
                aliquotaIr = new BigDecimal("17.50");
            } else {
                aliquotaIr = new BigDecimal("15.00");
            }
        }

        BigDecimal impostoRenda = rendimentoBruto.multiply(aliquotaIr).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal valorLiquido = valorBruto.subtract(impostoRenda);
        BigDecimal rendimentoLiquido = valorLiquido.subtract(valorInvestido);

        BigDecimal rentabilidadeLiquidaPct = PercentualUtils.calcularPercentual(rendimentoLiquido, valorInvestido);

        double ipcaAcumuladoDbl = (Math.pow(1.0 + (ipcaAno.doubleValue() / 100.0), anos) - 1.0) * 100.0;
        double ganhoRealDbl = ((1.0 + (rentabilidadeLiquidaPct.doubleValue() / 100.0)) / (1.0 + (ipcaAcumuladoDbl / 100.0)) - 1.0) * 100.0;
        BigDecimal ganhoRealPct = new BigDecimal(ganhoRealDbl).setScale(2, RoundingMode.HALF_UP);

        return SimulacaoResultadoDTO.builder()
                .nome(req.getNome() != null ? req.getNome() : "Simulação " + tipoUpper)
                .tipoAtivo(tipoUpper)
                .valorInvestido(valorInvestido)
                .valorBruto(valorBruto)
                .aliquotaIr(aliquotaIr)
                .valorImpostoRenda(impostoRenda)
                .valorLiquido(valorLiquido)
                .rendimentoBruto(rendimentoBruto)
                .rendimentoLiquido(rendimentoLiquido)
                .rentabilidadeLiquidaPercentual(rentabilidadeLiquidaPct)
                .ganhoRealPercentual(ganhoRealPct)
                .prazoMeses(prazoMeses)
                .taxaCdiUtilizada(cdiAno)
                .taxaIpcaUtilizada(ipcaAno)
                .build();
    }

    @Transactional
    public SimulacaoResultadoDTO salvarSimulacao(SimulacaoRequestDTO req) {
        SimulacaoResultadoDTO resultado = simular(req);
        try {
            String json = objectMapper.writeValueAsString(resultado);
            SimulacaoSalva sim = SimulacaoBuilder.umaSimulacao()
                    .comNome(resultado.getNome())
                    .comTipoAtivo(resultado.getTipoAtivo())
                    .comValorInvestido(resultado.getValorInvestido())
                    .comTaxa(req.getTaxaAno())
                    .comIndexador(req.getTipoAtivo())
                    .comResultadoJson(json)
                    .build();

            SimulacaoSalva salva = simulacaoRepository.save(sim);
            resultado.setId(salva.getId());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao salvar simulação: " + e.getMessage(), e);
        }
        return resultado;
    }

    public List<SimulacaoSalva> listarSimulacoesSalvas() {
        return simulacaoRepository.findAllByOrderByCriadoEmDesc();
    }

    public ComparacaoDTO comparar(SimulacaoRequestDTO req1, SimulacaoRequestDTO req2) {
        SimulacaoResultadoDTO res1 = simular(req1);
        SimulacaoResultadoDTO res2 = simular(req2);

        BigDecimal diferenca = res1.getValorLiquido().subtract(res2.getValorLiquido()).abs();
        String melhor = res1.getValorLiquido().compareTo(res2.getValorLiquido()) >= 0
                ? res1.getNome() : res2.getNome();

        return ComparacaoDTO.builder()
                .opcao1(res1)
                .opcao2(res2)
                .diferencaValorLiquido(diferenca)
                .melhorOpcao(melhor)
                .build();
    }
}
