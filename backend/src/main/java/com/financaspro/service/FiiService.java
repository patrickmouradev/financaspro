package com.financaspro.service;

import com.financaspro.builder.DividendoBuilder;
import com.financaspro.builder.FiiBuilder;
import com.financaspro.model.dto.DividendoDTO;
import com.financaspro.model.dto.FiiDTO;
import com.financaspro.model.entity.Dividendo;
import com.financaspro.model.entity.Fii;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.utils.MoedaUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FiiService {

    private final FiiRepository fiiRepository;
    private final DividendoRepository dividendoRepository;

    public FiiService(FiiRepository fiiRepository, DividendoRepository dividendoRepository) {
        this.fiiRepository = fiiRepository;
        this.dividendoRepository = dividendoRepository;
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

    public List<FiiDTO> listarFiis() {
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
