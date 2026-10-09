package com.financaspro.modulo.fundos.service;

import com.financaspro.model.dto.MetaDTO;
import com.financaspro.model.dto.ProjecaoMetaDTO;
import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.Operacao;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.repository.OperacaoRepository;
import com.financaspro.utils.MoedaUtils;
import com.financaspro.utils.PercentualUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MetaDividendoService {

    private static final Pattern PATTERN_TICKER = Pattern.compile("\\b([A-Z]{4}11[B]?)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_QTD = Pattern.compile("QTD:\\s*(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE);

    private final FiiRepository fiiRepository;
    private final DividendoRepository dividendoRepository;
    private final FiiService fiiService;
    private final LancamentoRepository lancamentoRepository;
    private final OperacaoRepository operacaoRepository;

    public MetaDividendoService(
            FiiRepository fiiRepository,
            DividendoRepository dividendoRepository,
            FiiService fiiService,
            LancamentoRepository lancamentoRepository,
            OperacaoRepository operacaoRepository) {
        this.fiiRepository = fiiRepository;
        this.dividendoRepository = dividendoRepository;
        this.fiiService = fiiService;
        this.lancamentoRepository = lancamentoRepository;
        this.operacaoRepository = operacaoRepository;
    }

    public ProjecaoMetaDTO calcularProgressoEMeta(BigDecimal metaMensal) {
        if (metaMensal == null || metaMensal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A meta de renda mensal deve ser maior que 0.");
        }

        fiiService.sincronizarFiisComLancamentosExistentes();
        List<Fii> fiis = fiiRepository.findAll();
        List<Lancamento> todosLancamentos = lancamentoRepository.findAll();
        LocalDate dozeMesesAtras = LocalDate.now().minusMonths(12);

        BigDecimal rendaMensalAtualTotal = BigDecimal.ZERO;
        List<ProjecaoMetaDTO.ProjecaoFiiItemDTO> itens = new ArrayList<>();

        for (Fii fii : fiis) {
            if (fii.getQuantidadeCotas() == null || fii.getQuantidadeCotas().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            List<Dividendo> dividendoList = dividendoRepository.findByFiiIdOrderByDataPagamentoDesc(fii.getId());
            List<Dividendo> recentes = dividendoList.stream()
                    .filter(d -> !d.getDataPagamento().isBefore(dozeMesesAtras))
                    .toList();

            BigDecimal divMedioPorCota = BigDecimal.ZERO;
            if (!recentes.isEmpty()) {
                BigDecimal somaPorCota = recentes.stream()
                        .map(Dividendo::getValorPorCota)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                divMedioPorCota = somaPorCota.divide(new BigDecimal(recentes.size()), 4, RoundingMode.HALF_UP);
            }

            if (divMedioPorCota.compareTo(BigDecimal.ZERO) == 0 && fii.getPrecoMedio() != null && fii.getPrecoMedio().compareTo(BigDecimal.ZERO) > 0) {
                divMedioPorCota = fii.getPrecoMedio().multiply(new BigDecimal("0.008")).setScale(4, RoundingMode.HALF_UP);
            }

            BigDecimal rendimentoMensalFii = fii.getQuantidadeCotas().multiply(divMedioPorCota);
            rendaMensalAtualTotal = rendaMensalAtualTotal.add(rendimentoMensalFii);

            BigDecimal pm = (fii.getPrecoMedio() != null) ? fii.getPrecoMedio() : BigDecimal.ZERO;
            BigDecimal precoAtual = pm;
            BigDecimal valorTotalInvestido = MoedaUtils.arredondar2Casas(fii.getQuantidadeCotas().multiply(pm));
            BigDecimal valorAtualTotal = MoedaUtils.arredondar2Casas(fii.getQuantidadeCotas().multiply(precoAtual));

            List<ProjecaoMetaDTO.OperacaoCompraItemDTO> compras = buscarComprasDoTicker(fii.getTicker(), todosLancamentos);

            itens.add(ProjecaoMetaDTO.ProjecaoFiiItemDTO.builder()
                    .fiiId(fii.getId())
                    .ticker(fii.getTicker())
                    .nome(fii.getNome())
                    .cotasAtuais(fii.getQuantidadeCotas())
                    .precoMedio(MoedaUtils.arredondar2Casas(pm))
                    .precoAtual(MoedaUtils.arredondar2Casas(precoAtual))
                    .valorTotalInvestido(valorTotalInvestido)
                    .valorAtualTotal(valorAtualTotal)
                    .dividendoMedioPorCota(divMedioPorCota)
                    .rendimentoMensalAtual(MoedaUtils.arredondar2Casas(rendimentoMensalFii))
                    .cotasFaltantesParaMetaTotal(BigDecimal.ZERO)
                    .investimentoEstimadoNecessario(BigDecimal.ZERO)
                    .compras(compras)
                    .build());
        }

        BigDecimal faltaParaMeta = metaMensal.subtract(rendaMensalAtualTotal);
        if (faltaParaMeta.compareTo(BigDecimal.ZERO) < 0) {
            faltaParaMeta = BigDecimal.ZERO;
        }

        BigDecimal percentualConcluido = PercentualUtils.calcularPercentual(rendaMensalAtualTotal, metaMensal);
        BigDecimal aporteTotalEstimado = BigDecimal.ZERO;

        long qtdAtivosValidos = itens.stream()
                .filter(i -> i.getDividendoMedioPorCota() != null && i.getDividendoMedioPorCota().compareTo(BigDecimal.ZERO) > 0)
                .count();

        if (faltaParaMeta.compareTo(BigDecimal.ZERO) > 0 && qtdAtivosValidos > 0) {
            BigDecimal faltaPorAtivo = faltaParaMeta.divide(new BigDecimal(qtdAtivosValidos), 4, RoundingMode.HALF_UP);

            for (ProjecaoMetaDTO.ProjecaoFiiItemDTO item : itens) {
                if (item.getDividendoMedioPorCota() != null && item.getDividendoMedioPorCota().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal cotasFaltantes = faltaPorAtivo.divide(item.getDividendoMedioPorCota(), 2, RoundingMode.HALF_UP);
                    Fii fii = fiiRepository.findById(item.getFiiId()).orElse(null);
                    BigDecimal preco = (fii != null && fii.getPrecoMedio() != null && fii.getPrecoMedio().compareTo(BigDecimal.ZERO) > 0)
                            ? fii.getPrecoMedio()
                            : new BigDecimal("100.00");
                    BigDecimal investimentoEstimado = cotasFaltantes.multiply(preco);

                    item.setCotasFaltantesParaMetaTotal(cotasFaltantes);
                    item.setInvestimentoEstimadoNecessario(MoedaUtils.arredondar2Casas(investimentoEstimado));
                    aporteTotalEstimado = aporteTotalEstimado.add(investimentoEstimado);
                }
            }
        }

        MetaDTO resumoMeta = MetaDTO.builder()
                .metaMensal(MoedaUtils.arredondar2Casas(metaMensal))
                .mediaRendaMensalAtual(MoedaUtils.arredondar2Casas(rendaMensalAtualTotal))
                .percentualConcluido(percentualConcluido)
                .faltaParaMetaMensal(MoedaUtils.arredondar2Casas(faltaParaMeta))
                .build();

        return ProjecaoMetaDTO.builder()
                .resumoMeta(resumoMeta)
                .projecoesPorFii(itens)
                .aporteTotalEstimadoNecessario(MoedaUtils.arredondar2Casas(aporteTotalEstimado))
                .build();
    }

    private List<ProjecaoMetaDTO.OperacaoCompraItemDTO> buscarComprasDoTicker(String ticker, List<Lancamento> todosLancamentos) {
        List<ProjecaoMetaDTO.OperacaoCompraItemDTO> compras = new ArrayList<>();

        List<Operacao> ops = operacaoRepository.findByAtivoTickerOrderByDataOperacaoAsc(ticker);
        if (ops != null && !ops.isEmpty()) {
            for (Operacao op : ops) {
                BigDecimal qtd = op.getQuantidade() != null ? op.getQuantidade() : BigDecimal.ONE;
                BigDecimal precoUnit = op.getPrecoUnitario() != null ? op.getPrecoUnitario() : BigDecimal.ZERO;
                BigDecimal taxas = op.getTaxas() != null ? op.getTaxas() : BigDecimal.ZERO;
                BigDecimal totalPago = MoedaUtils.arredondar2Casas(qtd.multiply(precoUnit).add(taxas));

                compras.add(ProjecaoMetaDTO.OperacaoCompraItemDTO.builder()
                        .id(op.getId())
                        .dataCompra(op.getDataOperacao())
                        .quantidade(qtd)
                        .precoUnitario(MoedaUtils.arredondar2Casas(precoUnit))
                        .taxasB3(MoedaUtils.arredondar2Casas(taxas))
                        .precoMedioAjustadoOperacao(MoedaUtils.arredondar2Casas(precoUnit))
                        .valorTotalPago(totalPago)
                        .origemOuNota(op.getObservacao() != null && !op.getObservacao().isBlank() ? op.getObservacao() : "Nota de Corretagem B3")
                        .build());
            }
            return compras;
        }

        String tickerUpper = ticker.toUpperCase().trim();
        for (Lancamento l : todosLancamentos) {
            if (l.getDescricao() == null) continue;
            String desc = l.getDescricao().toUpperCase();
            String origem = l.getOrigem() != null ? l.getOrigem().toUpperCase() : "";

            if (!desc.contains(tickerUpper)) continue;
            if (!origem.contains("NOTA") && !origem.contains("INVESTIMENTO") && !"COMPRA_ATIVO".equalsIgnoreCase(l.getTipo())) continue;

            BigDecimal qtd = BigDecimal.ONE;
            Matcher mQtd = PATTERN_QTD.matcher(desc);
            if (mQtd.find()) {
                try {
                    qtd = new BigDecimal(mQtd.group(1).replace(",", "."));
                } catch (Exception ignored) {}
            }

            BigDecimal valorTotal = l.getValor().abs();
            BigDecimal pmAjustado = qtd.compareTo(BigDecimal.ZERO) > 0
                    ? valorTotal.divide(qtd, 4, RoundingMode.HALF_UP)
                    : valorTotal;

            BigDecimal taxas = BigDecimal.ZERO;
            if (l.getObservacao() != null) {
                if (l.getObservacao().contains("Preço Médio Ajustado com Taxas B3: R$ ")) {
                    try {
                        int idx = l.getObservacao().indexOf("Preço Médio Ajustado com Taxas B3: R$ ");
                        String valStr = l.getObservacao().substring(idx + "Preço Médio Ajustado com Taxas B3: R$ ".length()).trim();
                        pmAjustado = new BigDecimal(valStr.replace(",", "."));
                    } catch (Exception ignored) {}
                }
                if (l.getObservacao().contains("Taxas B3: R$ ")) {
                    try {
                        int idx = l.getObservacao().indexOf("Taxas B3: R$ ");
                        int endIdx = l.getObservacao().indexOf("|", idx);
                        String valStr = (endIdx > idx)
                                ? l.getObservacao().substring(idx + "Taxas B3: R$ ".length(), endIdx).trim()
                                : l.getObservacao().substring(idx + "Taxas B3: R$ ".length()).trim();
                        taxas = new BigDecimal(valStr.replace(",", "."));
                    } catch (Exception ignored) {}
                }
            }

            BigDecimal precoUnit = (qtd.compareTo(BigDecimal.ZERO) > 0)
                    ? valorTotal.subtract(taxas).divide(qtd, 4, RoundingMode.HALF_UP)
                    : pmAjustado;
            if (precoUnit.compareTo(BigDecimal.ZERO) <= 0) {
                precoUnit = pmAjustado;
            }

            compras.add(ProjecaoMetaDTO.OperacaoCompraItemDTO.builder()
                    .id(l.getId())
                    .dataCompra(l.getDataLancamento().toLocalDate())
                    .quantidade(qtd)
                    .precoUnitario(MoedaUtils.arredondar2Casas(precoUnit))
                    .taxasB3(MoedaUtils.arredondar2Casas(taxas))
                    .precoMedioAjustadoOperacao(MoedaUtils.arredondar2Casas(pmAjustado))
                    .valorTotalPago(MoedaUtils.arredondar2Casas(valorTotal))
                    .origemOuNota(l.getOrigem() != null ? l.getOrigem() : "Nota de Corretagem")
                    .build());
        }

        return compras;
    }
}
