package com.financaspro.modulo.containvestimento.service;

import com.financaspro.model.dto.CarteiraDTO;
import com.financaspro.model.dto.RentabilidadeDTO;
import com.financaspro.model.entity.Ativo;
import com.financaspro.model.entity.Operacao;
import com.financaspro.model.enums.TipoOperacao;
import com.financaspro.repository.AtivoRepository;
import com.financaspro.repository.OperacaoRepository;
import com.financaspro.service.CotacaoMercadoService;
import com.financaspro.utils.MoedaUtils;
import com.financaspro.utils.PercentualUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentabilidadeService {

    private static final Logger log = LoggerFactory.getLogger(RentabilidadeService.class);

    private final AtivoRepository ativoRepository;
    private final OperacaoRepository operacaoRepository;
    private final CotacaoMercadoService cotacaoMercadoService;

    @Autowired
    public RentabilidadeService(AtivoRepository ativoRepository,
                                OperacaoRepository operacaoRepository,
                                CotacaoMercadoService cotacaoMercadoService) {
        this.ativoRepository = ativoRepository;
        this.operacaoRepository = operacaoRepository;
        this.cotacaoMercadoService = cotacaoMercadoService;
    }

    // Construtor de compatibilidade para testes legados
    public RentabilidadeService(AtivoRepository ativoRepository,
                                OperacaoRepository operacaoRepository,
                                RestTemplateBuilder restTemplateBuilder) {
        this(ativoRepository, operacaoRepository, new CotacaoMercadoService());
    }

    public CarteiraDTO calcularCarteira() {
        List<Ativo> ativos = ativoRepository.findAll();
        List<RentabilidadeDTO> itens = new ArrayList<>();

        BigDecimal valorTotalInvestido = BigDecimal.ZERO;
        BigDecimal valorTotalAtual = BigDecimal.ZERO;

        for (Ativo ativo : ativos) {
            RentabilidadeDTO rentabilidade = calcularRentabilidadeAtivo(ativo);
            if (rentabilidade != null && rentabilidade.getQuantidadeAtual().compareTo(BigDecimal.ZERO) > 0) {
                itens.add(rentabilidade);
                valorTotalInvestido = valorTotalInvestido.add(rentabilidade.getValorTotalInvestido());
                valorTotalAtual = valorTotalAtual.add(rentabilidade.getValorAtual());
            }
        }

        final BigDecimal totalAtualFinal = valorTotalAtual;
        for (RentabilidadeDTO item : itens) {
            if (totalAtualFinal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pct = PercentualUtils.calcularPercentual(item.getValorAtual(), totalAtualFinal);
                item.setPercentualCarteira(pct);
            } else {
                item.setPercentualCarteira(BigDecimal.ZERO);
            }
        }

        BigDecimal lucroPrejuizoTotal = valorTotalAtual.subtract(valorTotalInvestido);
        BigDecimal variacaoPercentualTotal = PercentualUtils.calcularVariacao(valorTotalAtual, valorTotalInvestido);

        return CarteiraDTO.builder()
                .valorTotalInvestido(MoedaUtils.arredondar2Casas(valorTotalInvestido))
                .valorTotalAtual(MoedaUtils.arredondar2Casas(valorTotalAtual))
                .lucroPrejuizoTotal(MoedaUtils.arredondar2Casas(lucroPrejuizoTotal))
                .variacaoPercentualTotal(variacaoPercentualTotal)
                .itens(itens)
                .build();
    }

    public RentabilidadeDTO calcularRentabilidadeAtivo(Ativo ativo) {
        List<Operacao> operacoes = operacaoRepository.findByAtivoIdOrderByDataOperacaoAsc(ativo.getId());
        if (operacoes.isEmpty()) {
            return null;
        }

        BigDecimal quantidadeAtual = BigDecimal.ZERO;
        BigDecimal custoTotal = BigDecimal.ZERO;
        BigDecimal precoMedio = BigDecimal.ZERO;

        for (Operacao op : operacoes) {
            BigDecimal qtdOp = op.getQuantidade();
            BigDecimal precoOp = op.getPrecoUnitario();
            BigDecimal taxasOp = op.getTaxas() != null ? op.getTaxas() : BigDecimal.ZERO;

            if (op.getTipo() == TipoOperacao.COMPRA) {
                BigDecimal custoCompra = qtdOp.multiply(precoOp).add(taxasOp);
                custoTotal = custoTotal.add(custoCompra);
                quantidadeAtual = quantidadeAtual.add(qtdOp);
                if (quantidadeAtual.compareTo(BigDecimal.ZERO) > 0) {
                    precoMedio = custoTotal.divide(quantidadeAtual, 6, RoundingMode.HALF_UP);
                }
            } else if (op.getTipo() == TipoOperacao.VENDA) {
                quantidadeAtual = quantidadeAtual.subtract(qtdOp);
                if (quantidadeAtual.compareTo(BigDecimal.ZERO) <= 0) {
                    quantidadeAtual = BigDecimal.ZERO;
                    custoTotal = BigDecimal.ZERO;
                    precoMedio = BigDecimal.ZERO;
                } else {
                    custoTotal = precoMedio.multiply(quantidadeAtual);
                }
            }
        }

        if (quantidadeAtual.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        // Busca cotação atualizada na internet via Yahoo Finance / BRAPI
        BigDecimal precoMercado = cotacaoMercadoService.obterCotacaoAtual(ativo.getTicker());
        boolean cotacaoReal = (precoMercado != null && precoMercado.compareTo(BigDecimal.ZERO) > 0);
        BigDecimal precoAtual = cotacaoReal ? precoMercado : precoMedio;

        BigDecimal valorTotalInvestido = quantidadeAtual.multiply(precoMedio);
        BigDecimal valorAtual = quantidadeAtual.multiply(precoAtual);
        BigDecimal lucroPrejuizo = valorAtual.subtract(valorTotalInvestido);
        BigDecimal variacao = PercentualUtils.calcularVariacao(valorAtual, valorTotalInvestido);

        Long diasAteVencimento = null;
        String statusVencimento = null;
        if (ativo.getDataVencimento() != null) {
            diasAteVencimento = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), ativo.getDataVencimento());
            if (java.time.LocalDate.now().isAfter(ativo.getDataVencimento())) {
                statusVencimento = "VENCIDO";
            } else {
                statusVencimento = "EM_ANDAMENTO";
            }
        }

        return RentabilidadeDTO.builder()
                .ativoId(ativo.getId())
                .ticker(ativo.getTicker())
                .nomeAtivo(ativo.getNome())
                .tipoAtivo(ativo.getTipo())
                .setor(ativo.getSetor())
                .classe(ativo.getClasse())
                .indexador(ativo.getIndexador())
                .quantidadeAtual(quantidadeAtual)
                .precoMedio(MoedaUtils.arredondar2Casas(precoMedio))
                .precoAtual(MoedaUtils.arredondar2Casas(precoAtual))
                .valorTotalInvestido(MoedaUtils.arredondar2Casas(valorTotalInvestido))
                .valorAtual(MoedaUtils.arredondar2Casas(valorAtual))
                .lucroPrejuizo(MoedaUtils.arredondar2Casas(lucroPrejuizo))
                .variacaoPercentual(variacao)
                .percentualCarteira(BigDecimal.ZERO)
                .cotacaoEmTempoReal(cotacaoReal)
                .dataAplicacao(ativo.getDataAplicacao())
                .dataVencimento(ativo.getDataVencimento())
                .liquidez(ativo.getLiquidez())
                .porcentagemTaxa(ativo.getPorcentagemTaxa())
                .emissor(ativo.getEmissor())
                .cnpjEmissor(ativo.getCnpjEmissor())
                .produto(ativo.getProduto() != null ? ativo.getProduto() : ativo.getClasse())
                .diasAteVencimento(diasAteVencimento)
                .statusVencimento(statusVencimento)
                .build();
    }
}
