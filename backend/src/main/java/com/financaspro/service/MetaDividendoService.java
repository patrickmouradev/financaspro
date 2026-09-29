package com.financaspro.service;

import com.financaspro.model.dto.MetaDTO;
import com.financaspro.model.dto.ProjecaoMetaDTO;
import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.utils.MoedaUtils;
import com.financaspro.utils.PercentualUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class MetaDividendoService {

    private final FiiRepository fiiRepository;
    private final DividendoRepository dividendoRepository;

    public MetaDividendoService(FiiRepository fiiRepository, DividendoRepository dividendoRepository) {
        this.fiiRepository = fiiRepository;
        this.dividendoRepository = dividendoRepository;
    }

    public ProjecaoMetaDTO calcularProgressoEMeta(BigDecimal metaMensal) {
        if (metaMensal == null || metaMensal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("A meta de renda mensal deve ser maior que 0.");
        }

        List<Fii> fiis = fiiRepository.findAll();
        LocalDate dozeMesesAtras = LocalDate.now().minusMonths(12);

        BigDecimal rendaMensalAtualTotal = BigDecimal.ZERO;
        List<ProjecaoMetaDTO.ProjecaoFiiItemDTO> itens = new ArrayList<>();

        for (Fii fii : fiis) {
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

            BigDecimal rendimentoMensalFii = fii.getQuantidadeCotas().multiply(divMedioPorCota);
            rendaMensalAtualTotal = rendaMensalAtualTotal.add(rendimentoMensalFii);

            itens.add(ProjecaoMetaDTO.ProjecaoFiiItemDTO.builder()
                    .fiiId(fii.getId())
                    .ticker(fii.getTicker())
                    .nome(fii.getNome())
                    .cotasAtuais(fii.getQuantidadeCotas())
                    .dividendoMedioPorCota(divMedioPorCota)
                    .rendimentoMensalAtual(MoedaUtils.arredondar2Casas(rendimentoMensalFii))
                    .cotasFaltantesParaMetaTotal(BigDecimal.ZERO)
                    .investimentoEstimadoNecessario(BigDecimal.ZERO)
                    .build());
        }

        BigDecimal faltaParaMeta = metaMensal.subtract(rendaMensalAtualTotal);
        if (faltaParaMeta.compareTo(BigDecimal.ZERO) < 0) {
            faltaParaMeta = BigDecimal.ZERO;
        }

        BigDecimal percentualConcluido = PercentualUtils.calcularPercentual(rendaMensalAtualTotal, metaMensal);
        BigDecimal aporteTotalEstimado = BigDecimal.ZERO;

        for (ProjecaoMetaDTO.ProjecaoFiiItemDTO item : itens) {
            if (faltaParaMeta.compareTo(BigDecimal.ZERO) > 0 && item.getDividendoMedioPorCota().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal cotasFaltantes = faltaParaMeta.divide(item.getDividendoMedioPorCota(), 2, RoundingMode.HALF_UP);
                Fii fii = fiiRepository.findById(item.getFiiId()).orElse(null);
                BigDecimal preco = fii != null && fii.getPrecoMedio().compareTo(BigDecimal.ZERO) > 0 ? fii.getPrecoMedio() : new BigDecimal("100.00");
                BigDecimal investimentoEstimado = cotasFaltantes.multiply(preco);

                item.setCotasFaltantesParaMetaTotal(cotasFaltantes);
                item.setInvestimentoEstimadoNecessario(MoedaUtils.arredondar2Casas(investimentoEstimado));
                aporteTotalEstimado = aporteTotalEstimado.add(investimentoEstimado);
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
}
