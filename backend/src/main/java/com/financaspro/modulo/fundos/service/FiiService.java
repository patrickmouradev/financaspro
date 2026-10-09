package com.financaspro.modulo.fundos.service;

import com.financaspro.builder.DividendoBuilder;
import com.financaspro.builder.FiiBuilder;
import com.financaspro.model.dto.DividendoDTO;
import com.financaspro.model.dto.FiiDTO;
import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.Operacao;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.repository.OperacaoRepository;
import com.financaspro.utils.MoedaUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class FiiService {

    private static final Pattern PATTERN_TICKER_FII = Pattern.compile("\\b([A-Z]{4}11[B]?)\\b", Pattern.CASE_INSENSITIVE);

    private final FiiRepository fiiRepository;
    private final DividendoRepository dividendoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final OperacaoRepository operacaoRepository;

    public FiiService(
            FiiRepository fiiRepository,
            DividendoRepository dividendoRepository,
            LancamentoRepository lancamentoRepository,
            OperacaoRepository operacaoRepository) {
        this.fiiRepository = fiiRepository;
        this.dividendoRepository = dividendoRepository;
        this.lancamentoRepository = lancamentoRepository;
        this.operacaoRepository = operacaoRepository;
    }

    @Transactional
    public FiiDTO cadastrarFii(FiiDTO dto) {
        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (fiiRepository.existsByTicker(tickerUpper)) {
            throw new IllegalArgumentException("FII com ticker " + tickerUpper + " já cadastrado.");
        }

        Fii fii = FiiBuilder.umFii()
                .comTicker(tickerUpper)
                .comNome(dto.getNome())
                .comSegmento(dto.getSegmento())
                .comQuantidadeCotas(dto.getQuantidadeCotas() != null ? dto.getQuantidadeCotas() : BigDecimal.ZERO)
                .comPrecoMedio(dto.getPrecoMedio() != null ? dto.getPrecoMedio() : BigDecimal.ZERO)
                .build();

        Fii salvo = fiiRepository.save(fii);
        return converterParaFiiDTO(salvo);
    }

    @Transactional
    public FiiDTO atualizarFii(Long id, FiiDTO dto) {
        Fii fii = fiiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FII não encontrado com id: " + id));

        fii.setNome(dto.getNome());
        fii.setSegmento(dto.getSegmento());
        if (dto.getQuantidadeCotas() != null) fii.setQuantidadeCotas(dto.getQuantidadeCotas());
        if (dto.getPrecoMedio() != null) fii.setPrecoMedio(dto.getPrecoMedio());

        Fii salvo = fiiRepository.save(fii);
        return converterParaFiiDTO(salvo);
    }

    @Transactional
    public List<FiiDTO> listarFiis() {
        sincronizarFiisComLancamentosExistentes();
        return fiiRepository.findAll().stream()
                .map(this::converterParaFiiDTO)
                .collect(Collectors.toList());
    }

    public FiiDTO buscarFiiPorId(Long id) {
        Fii fii = fiiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FII não encontrado com id: " + id));
        return converterParaFiiDTO(fii);
    }

    @Transactional
    public void deletarFii(Long id) {
        if (!fiiRepository.existsById(id)) {
            throw new IllegalArgumentException("FII não encontrado com id: " + id);
        }
        fiiRepository.deleteById(id);
    }

    @Transactional
    public DividendoDTO registrarDividendo(DividendoDTO dto) {
        Fii fii = fiiRepository.findById(dto.getFiiId())
                .orElseThrow(() -> new IllegalArgumentException("FII não encontrado com id: " + dto.getFiiId()));

        BigDecimal qtdCotas = dto.getQuantidadeCotas() != null ? dto.getQuantidadeCotas() : fii.getQuantidadeCotas();
        BigDecimal valorTotal = dto.getValorTotal() != null ? dto.getValorTotal() : qtdCotas.multiply(dto.getValorPorCota());

        Dividendo dividendo = DividendoBuilder.umDividendo()
                .comFii(fii)
                .comDataPagamento(dto.getDataPagamento())
                .comValorPorCota(dto.getValorPorCota())
                .comQuantidadeCotas(qtdCotas)
                .comValorTotal(valorTotal)
                .build();

        Dividendo salvo = dividendoRepository.save(dividendo);
        return converterParaDividendoDTO(salvo);
    }

    public List<DividendoDTO> listarDividendosPorFii(Long fiiId) {
        return dividendoRepository.findByFiiIdOrderByDataPagamentoDesc(fiiId).stream()
                .map(this::converterParaDividendoDTO)
                .collect(Collectors.toList());
    }

    public List<DividendoDTO> listarTodosDividendos() {
        return dividendoRepository.findAllByOrderByDataPagamentoDesc().stream()
                .map(this::converterParaDividendoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletarDividendo(Long id) {
        if (!dividendoRepository.existsById(id)) {
            throw new IllegalArgumentException("Dividendo não encontrado com id: " + id);
        }
        dividendoRepository.deleteById(id);
    }

    @Transactional
    public void sincronizarFiisComLancamentosExistentes() {
        List<Lancamento> todos = lancamentoRepository.findAll();

        Map<String, BigDecimal> cotasPorTicker = new HashMap<>();
        Map<String, BigDecimal> custoPorTicker = new HashMap<>();

        for (Lancamento l : todos) {
            if (l.getDescricao() == null) continue;
            String desc = l.getDescricao().toUpperCase();
            String origem = l.getOrigem() != null ? l.getOrigem().toUpperCase() : "";

            Matcher mTicker = PATTERN_TICKER_FII.matcher(desc);
            if (mTicker.find()) {
                String ticker = mTicker.group(1).toUpperCase().trim();

                BigDecimal qtd = BigDecimal.ONE;
                Matcher mQtd = Pattern.compile("(?:QTD|Qtd|quantidade)[:\\s]*(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE).matcher(desc);
                if (mQtd.find()) {
                    try {
                        qtd = new BigDecimal(mQtd.group(1).replace(",", "."));
                    } catch (Exception ignored) {}
                }

                BigDecimal pm = l.getValor().abs();
                if (qtd.compareTo(BigDecimal.ZERO) > 0) {
                    pm = l.getValor().abs().divide(qtd, 4, RoundingMode.HALF_UP);
                }

                if (l.getObservacao() != null && l.getObservacao().contains("Preço Médio Ajustado com Taxas B3: R$ ")) {
                    try {
                        int idx = l.getObservacao().indexOf("Preço Médio Ajustado com Taxas B3: R$ ");
                        String valStr = l.getObservacao().substring(idx + "Preço Médio Ajustado com Taxas B3: R$ ".length()).trim();
                        pm = new BigDecimal(valStr.replace(",", "."));
                    } catch (Exception ignored) {}
                }

                BigDecimal qtdAtual = cotasPorTicker.getOrDefault(ticker, BigDecimal.ZERO);
                BigDecimal custoAtual = custoPorTicker.getOrDefault(ticker, BigDecimal.ZERO);

                cotasPorTicker.put(ticker, qtdAtual.add(qtd));
                custoPorTicker.put(ticker, custoAtual.add(qtd.multiply(pm)));
            }
        }

        List<Fii> fiisExistentes = fiiRepository.findAll();
        for (Fii fii : fiisExistentes) {
            String ticker = fii.getTicker().toUpperCase().trim();
            List<Operacao> ops = operacaoRepository.findByAtivoTickerOrderByDataOperacaoAsc(ticker);
            if (ops != null && !ops.isEmpty()) {
                BigDecimal opQtdTotal = BigDecimal.ZERO;
                BigDecimal opCustoTotal = BigDecimal.ZERO;
                for (Operacao op : ops) {
                    BigDecimal q = op.getQuantidade() != null ? op.getQuantidade() : BigDecimal.ONE;
                    BigDecimal p = op.getPrecoUnitario() != null ? op.getPrecoUnitario() : BigDecimal.ZERO;
                    BigDecimal t = op.getTaxas() != null ? op.getTaxas() : BigDecimal.ZERO;
                    opQtdTotal = opQtdTotal.add(q);
                    opCustoTotal = opCustoTotal.add(q.multiply(p).add(t));
                }
                if (opQtdTotal.compareTo(BigDecimal.ZERO) > 0) {
                    cotasPorTicker.put(ticker, opQtdTotal);
                    custoPorTicker.put(ticker, opCustoTotal);
                }
            }
        }

        for (Map.Entry<String, BigDecimal> entry : cotasPorTicker.entrySet()) {
            String ticker = entry.getKey();
            BigDecimal qtdTotal = entry.getValue();
            BigDecimal custoTotal = custoPorTicker.getOrDefault(ticker, BigDecimal.ZERO);
            BigDecimal pmPonderado = qtdTotal.compareTo(BigDecimal.ZERO) > 0
                    ? custoTotal.divide(qtdTotal, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            Fii fii = fiiRepository.findByTicker(ticker).orElseGet(() -> {
                return Fii.builder()
                        .ticker(ticker)
                        .nome("FII " + ticker)
                        .segmento("Imobiliário")
                        .quantidadeCotas(BigDecimal.ZERO)
                        .precoMedio(BigDecimal.ZERO)
                        .build();
            });

            fii.setQuantidadeCotas(qtdTotal);
            fii.setPrecoMedio(pmPonderado);
            fiiRepository.save(fii);
        }
    }

    public FiiDTO converterParaFiiDTO(Fii fii) {
        BigDecimal totalInvestido = fii.getQuantidadeCotas().multiply(fii.getPrecoMedio());
        return FiiDTO.builder()
                .id(fii.getId())
                .ticker(fii.getTicker())
                .nome(fii.getNome())
                .segmento(fii.getSegmento())
                .quantidadeCotas(fii.getQuantidadeCotas())
                .precoMedio(fii.getPrecoMedio())
                .valorTotalInvestido(MoedaUtils.arredondar2Casas(totalInvestido))
                .criadoEm(fii.getCriadoEm())
                .build();
    }

    public DividendoDTO converterParaDividendoDTO(Dividendo div) {
        return DividendoDTO.builder()
                .id(div.getId())
                .fiiId(div.getFii().getId())
                .tickerFii(div.getFii().getTicker())
                .nomeFii(div.getFii().getNome())
                .dataPagamento(div.getDataPagamento())
                .valorPorCota(div.getValorPorCota())
                .quantidadeCotas(div.getQuantidadeCotas())
                .valorTotal(div.getValorTotal())
                .criadoEm(div.getCriadoEm())
                .build();
    }
}
