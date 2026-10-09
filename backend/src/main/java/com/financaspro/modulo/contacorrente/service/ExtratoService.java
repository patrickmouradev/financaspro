package com.financaspro.modulo.contacorrente.service;

import com.financaspro.builder.LancamentoBuilder;
import com.financaspro.model.dto.CategoriaResumoDTO;
import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.model.dto.ResumoMensalDTO;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.ContaBancaria;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.ContaBancariaRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.service.ParametroService;
import com.financaspro.utils.DateUtils;

import com.financaspro.utils.ExcelGenerator;
import com.financaspro.utils.MoedaUtils;
import com.financaspro.utils.PdfGenerator;
import com.financaspro.utils.PercentualUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExtratoService {

    private final LancamentoRepository lancamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final ParametroService parametroService;

    public ExtratoService(
            LancamentoRepository lancamentoRepository,
            CategoriaRepository categoriaRepository,
            ContaBancariaRepository contaBancariaRepository,
            ParametroService parametroService) {
        this.lancamentoRepository = lancamentoRepository;
        this.categoriaRepository = categoriaRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.parametroService = parametroService;
    }

    public List<String> listarMesesDisponiveis() {
        List<LocalDateTime> datas = lancamentoRepository.findAllDataLancamento();
        java.util.Set<String> setMeses = new java.util.LinkedHashSet<>();
        for (LocalDateTime d : datas) {
            setMeses.add(YearMonth.from(d).toString());
        }
        if (setMeses.isEmpty()) {
            YearMonth cur = YearMonth.now();
            for (int i = 0; i < 12; i++) {
                setMeses.add(cur.minusMonths(i).toString());
            }
        }
        return new ArrayList<>(setMeses);
    }

    public YearMonth resolverAnoMes(YearMonth anoMes) {
        if (anoMes != null) {
            return anoMes;
        }
        YearMonth agora = YearMonth.now();
        LocalDateTime inicio = DateUtils.primeiroDiaMes(agora).atStartOfDay();
        LocalDateTime fim = DateUtils.ultimoDiaMes(agora).atTime(23, 59, 59);

        List<Lancamento> lancsHoje = lancamentoRepository.findByDataLancamentoBetweenOrderByDataLancamentoDesc(inicio, fim);
        if (!lancsHoje.isEmpty()) {
            return agora;
        }

        Optional<LocalDateTime> maxData = lancamentoRepository.findMaxDataLancamento();
        if (maxData.isPresent()) {
            return YearMonth.from(maxData.get());
        }

        return agora;
    }

    public List<LancamentoDTO> listarLancamentosPorMes(YearMonth anoMes) {
        YearMonth ym = resolverAnoMes(anoMes);
        LocalDateTime inicio = DateUtils.primeiroDiaMes(ym).atStartOfDay();
        LocalDateTime fim = DateUtils.ultimoDiaMes(ym).atTime(23, 59, 59);

        List<Lancamento> lancamentos = lancamentoRepository.findByDataLancamentoBetweenOrderByDataLancamentoDesc(inicio, fim);
        return lancamentos.stream()
                .map(LancamentoBuilder::paraDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LancamentoDTO atualizar(Long id, LancamentoDTO dto) {
        Lancamento entity = lancamentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lançamento não encontrado: " + id));

        if (dto.getDescricao() != null) {
            entity.setDescricao(dto.getDescricao());
        }
        if (dto.getValor() != null) {
            entity.setValor(dto.getValor());
        }
        if (dto.getCategoriaId() != null) {
            Optional<Categoria> catOpt = categoriaRepository.findById(dto.getCategoriaId());
            catOpt.ifPresent(entity::setCategoria);
            entity.setStatusCategorizacao("MANUAL");
        }
        if (dto.getContaBancariaId() != null) {
            Optional<ContaBancaria> contaOpt = contaBancariaRepository.findById(dto.getContaBancariaId());
            contaOpt.ifPresent(entity::setContaBancaria);
        }
        if (dto.getObservacao() != null) {
            entity.setObservacao(dto.getObservacao());
        }

        Lancamento salvo = lancamentoRepository.save(entity);
        return LancamentoBuilder.paraDTO(salvo);
    }

    @Transactional
    public void deletar(Long id) {
        lancamentoRepository.deleteById(id);
    }

    public ResumoMensalDTO resumoMensal(YearMonth anoMes) {
        YearMonth ym = resolverAnoMes(anoMes);
        LocalDateTime inicio = DateUtils.primeiroDiaMes(ym).atStartOfDay();
        LocalDateTime fim = DateUtils.ultimoDiaMes(ym).atTime(23, 59, 59);

        List<Lancamento> lancamentos = lancamentoRepository.findByDataLancamentoBetweenOrderByDataLancamentoDesc(inicio, fim);

        BigDecimal totalGasto = BigDecimal.ZERO;
        BigDecimal totalReceita = BigDecimal.ZERO;

        for (Lancamento l : lancamentos) {
            if (l.getValor() != null) {
                boolean ehReceita = (l.getCategoria() != null && "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
                        || (l.getValor().compareTo(BigDecimal.ZERO) > 0);
                if (ehReceita) {
                    totalReceita = totalReceita.add(l.getValor().abs());
                } else {
                    totalGasto = totalGasto.add(l.getValor().abs());
                }
            }
        }

        totalGasto = MoedaUtils.arredondar2Casas(totalGasto);
        totalReceita = MoedaUtils.arredondar2Casas(totalReceita);
        BigDecimal saldo = totalReceita.subtract(totalGasto);

        String metaStr = parametroService.buscarValorPorChave("gastos_mensais_meta", "5000.00");
        BigDecimal metaGastos = new BigDecimal(metaStr);

        BigDecimal percentualMeta = PercentualUtils.calcularPercentual(totalGasto, metaGastos);

        List<Object[]> sumCategorias = lancamentoRepository.sumByCategoriaAndPeriodo(inicio, fim);
        List<CategoriaResumoDTO> porCategoria = new ArrayList<>();

        for (Object[] row : sumCategorias) {
            Long catId = (Long) row[0];
            String catNome = (String) row[1];
            String catCor = (String) row[2];
            BigDecimal catValor = MoedaUtils.arredondar2Casas((BigDecimal) row[3]);
            BigDecimal catPerc = PercentualUtils.calcularPercentual(catValor, totalGasto);

            CategoriaResumoDTO cDto = CategoriaResumoDTO.builder()
                    .categoriaId(catId)
                    .categoriaNome(catNome)
                    .categoriaCor(catCor)
                    .valorTotal(catValor)
                    .percentualDoTotal(catPerc)
                    .build();
            porCategoria.add(cDto);
        }

        return ResumoMensalDTO.builder()
                .anoMes(ym.toString())
                .totalGasto(totalGasto)
                .totalReceita(totalReceita)
                .saldoMes(saldo)
                .metaGastos(metaGastos)
                .percentualMetaAtingido(percentualMeta)
                .porCategoria(porCategoria)
                .build();
    }

    public byte[] exportarPdf(YearMonth anoMes) throws Exception {
        ResumoMensalDTO resumo = resumoMensal(anoMes);
        List<LancamentoDTO> lancamentos = listarLancamentosPorMes(anoMes);

        StringBuilder sb = new StringBuilder();
        sb.append("Resumo do Mês: ").append(resumo.getAnoMes()).append("\n");
        sb.append("Total Gasto: ").append(MoedaUtils.formatarReal(resumo.getTotalGasto())).append("\n");
        sb.append("Total Receita: ").append(MoedaUtils.formatarReal(resumo.getTotalReceita())).append("\n");
        sb.append("Saldo Mês: ").append(MoedaUtils.formatarReal(resumo.getSaldoMes())).append("\n");
        sb.append("Meta de Gastos: ").append(MoedaUtils.formatarReal(resumo.getMetaGastos())).append(" (").append(resumo.getPercentualMetaAtingido()).append("%)\n\n");

        sb.append("--- Lançamentos ---\n");
        for (LancamentoDTO l : lancamentos) {
            sb.append(DateUtils.formatarDataBr(l.getDataLancamento().toLocalDate()))
              .append(" | ").append(l.getDescricao())
              .append(" | ").append(l.getCategoriaNome())
              .append(" | ").append(MoedaUtils.formatarReal(l.getValor()))
              .append("\n");
        }

        return PdfGenerator.gerarRelatorioSimples("Relatório de Extrato & Gastos — FinançasPro", sb.toString());
    }

    public byte[] exportarExcel(YearMonth anoMes) throws Exception {
        List<LancamentoDTO> lancamentos = listarLancamentosPorMes(anoMes);

        List<String> cabecalho = List.of("Data", "Descrição", "Categoria", "Valor", "Tipo", "Origem", "Observação");
        List<List<String>> dados = new ArrayList<>();

        for (LancamentoDTO l : lancamentos) {
            List<String> linha = List.of(
                    DateUtils.formatarDataBr(l.getDataLancamento().toLocalDate()),
                    l.getDescricao() != null ? l.getDescricao() : "",
                    l.getCategoriaNome() != null ? l.getCategoriaNome() : "",
                    l.getValor() != null ? l.getValor().toString() : "0.00",
                    l.getTipo() != null ? l.getTipo() : "",
                    l.getOrigem() != null ? l.getOrigem() : "",
                    l.getObservacao() != null ? l.getObservacao() : ""
            );
            dados.add(linha);
        }

        return ExcelGenerator.gerarPlanilha("Extrato " + anoMes.toString(), cabecalho, dados);
    }
}
