package com.financaspro.modulo.fundos.service;

import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.modulo.containvestimento.parser.BtgExtratoInvestimentoParser.ItemExtratoInvestimento;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DividendoReconciliacaoService {

    private final FiiRepository fiiRepository;
    private final DividendoRepository dividendoRepository;

    public DividendoReconciliacaoService(FiiRepository fiiRepository, DividendoRepository dividendoRepository) {
        this.fiiRepository = fiiRepository;
        this.dividendoRepository = dividendoRepository;
    }

    @Transactional
    public List<Dividendo> reconciliarEProcessarProventos(List<ItemExtratoInvestimento> itensInvestimento) {
        List<Dividendo> dividendosProcessados = new ArrayList<>();

        for (ItemExtratoInvestimento item : itensInvestimento) {
            if (!"DIVIDENDO".equalsIgnoreCase(item.getTipo()) && !"RENDIMENTO".equalsIgnoreCase(item.getTipo())) {
                continue;
            }

            String ticker = item.getTicker();
            if (ticker == null || ticker.trim().isEmpty()) {
                continue;
            }

            Fii fii = fiiRepository.findByTicker(ticker.toUpperCase().trim())
                    .orElseGet(() -> {
                        Fii novoFii = Fii.builder()
                                .ticker(ticker.toUpperCase().trim())
                                .nome("FII " + ticker.toUpperCase().trim())
                                .quantidadeCotas(BigDecimal.ONE)
                                .precoMedio(BigDecimal.ZERO)
                                .build();
                        return fiiRepository.save(novoFii);
                    });

            LocalDate dataPagamento = item.getDataMovimento().toLocalDate();
            BigDecimal valorTotal = item.getValor();

            BigDecimal quantidadeCotas = (fii.getQuantidadeCotas() != null && fii.getQuantidadeCotas().compareTo(BigDecimal.ZERO) > 0)
                    ? fii.getQuantidadeCotas()
                    : BigDecimal.ONE;

            BigDecimal valorPorCota = valorTotal.divide(quantidadeCotas, 4, RoundingMode.HALF_UP);

            Dividendo dividendo = Dividendo.builder()
                    .fii(fii)
                    .dataPagamento(dataPagamento)
                    .quantidadeCotas(quantidadeCotas)
                    .valorPorCota(valorPorCota)
                    .valorTotal(valorTotal)
                    .build();

            Dividendo salvo = dividendoRepository.save(dividendo);
            dividendosProcessados.add(salvo);
        }

        return dividendosProcessados;
    }
}
