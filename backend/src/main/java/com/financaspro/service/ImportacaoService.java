package com.financaspro.service;

import com.financaspro.builder.LancamentoBuilder;

import com.financaspro.model.dto.ImportacaoResultadoDTO;
import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.ContaBancaria;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.ContaBancariaRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.service.parser.BtgExtratoParser;
import com.financaspro.service.parser.BtgFaturaParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

@Service
public class ImportacaoService {

    private final BtgFaturaParser btgFaturaParser;
    private final BtgExtratoParser btgExtratoParser;
    private final CategorizacaoService categorizacaoService;
    private final ParametroService parametroService;
    private final LancamentoRepository lancamentoRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final CategoriaRepository categoriaRepository;

    public ImportacaoService(
            BtgFaturaParser btgFaturaParser,
            BtgExtratoParser btgExtratoParser,
            CategorizacaoService categorizacaoService,
            ParametroService parametroService,
            LancamentoRepository lancamentoRepository,
            ContaBancariaRepository contaBancariaRepository,
            CategoriaRepository categoriaRepository) {
        this.btgFaturaParser = btgFaturaParser;
        this.btgExtratoParser = btgExtratoParser;
        this.categorizacaoService = categorizacaoService;
        this.parametroService = parametroService;
        this.lancamentoRepository = lancamentoRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public ImportacaoResultadoDTO processarArquivo(InputStream inputStream, String nomeArquivo, Long contaBancariaId) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        List<Lancamento> lancamentosBrutos = new ArrayList<>();

        if (btgFaturaParser.ehFormatoBtgFatura(bytes, nomeArquivo)) {
            String senhaBtg = parametroService.buscarValorPorChave("senha_fatura_btg", "");
            if (senhaBtg.isEmpty()) {
                senhaBtg = parametroService.buscarValorPorChave("senha_arquivo_default", "");
            }
            lancamentosBrutos = btgFaturaParser.parsearFatura(bytes, senhaBtg);
        } else if (btgExtratoParser.ehFormatoBtgExtrato(bytes, nomeArquivo)) {
            lancamentosBrutos = btgExtratoParser.parsearExtrato(bytes);
        } else {
            throw new IllegalArgumentException("Formato de arquivo não reconhecido ou banco não suportado.");
        }

        Optional<ContaBancaria> contaOpt = (contaBancariaId != null) ? contaBancariaRepository.findById(contaBancariaId) : Optional.empty();

        int totalLidos = lancamentosBrutos.size();
        int autoCat = 0;
        int pendentes = 0;
        int ignoradosDeduplicacao = 0;

        List<LancamentoDTO> dtosPreview = new ArrayList<>();
        List<String> alertas = new ArrayList<>();

        for (Lancamento l : lancamentosBrutos) {
            if (contaOpt.isPresent()) {
                l.setContaBancaria(contaOpt.get());
            } else if (l.getObservacao() != null && l.getObservacao().contains("Final Cartão:")) {
                String finalCartao = l.getObservacao().substring(l.getObservacao().indexOf("Final Cartão:") + 13).trim();
                Optional<ContaBancaria> contaCartaoOpt = contaBancariaRepository.findByFinalCartaoAndAtivoTrue(finalCartao);
                contaCartaoOpt.ifPresent(l::setContaBancaria);
            }

            // Checa deduplicação contra o banco
            boolean duplicado = false;
            if (l.getCodigoAutorizacao() != null && !l.getCodigoAutorizacao().isEmpty()) {
                Optional<Lancamento> dupOpt = lancamentoRepository.findByCodigoAutorizacaoAndDataLancamentoAndValor(
                        l.getCodigoAutorizacao(), l.getDataLancamento(), l.getValor());
                if (dupOpt.isPresent()) {
                    duplicado = true;
                }
            } else {
                Optional<Lancamento> dupOpt = lancamentoRepository.findByDescricaoAndDataLancamentoAndValor(
                        l.getDescricao(), l.getDataLancamento(), l.getValor());
                if (dupOpt.isPresent()) {
                    duplicado = true;
                }
            }

            if (duplicado) {
                ignoradosDeduplicacao++;
                continue;
            }

            // Categorização automática
            categorizacaoService.categorizarLancamento(l);
            if ("AUTO".equalsIgnoreCase(l.getStatusCategorizacao())) {
                autoCat++;
            } else {
                pendentes++;
            }

            dtosPreview.add(LancamentoBuilder.paraDTO(l));
        }

        if (ignoradosDeduplicacao > 0) {
            alertas.add(ignoradosDeduplicacao + " lançamentos já existentes foram ignorados automaticamente por deduplicação.");
        }

        return ImportacaoResultadoDTO.builder()
                .totalLidos(totalLidos)
                .categorizadosAutomaticos(autoCat)
                .pendentesCategorizacao(pendentes)
                .ignoradosDeduplicacao(ignoradosDeduplicacao)
                .lancamentos(dtosPreview)
                .alertas(alertas)
                .build();
    }

    @Transactional
    public List<LancamentoDTO> confirmarImportacao(List<LancamentoDTO> dtosConfirmados) {
        List<LancamentoDTO> salvosDTO = new ArrayList<>();

        for (LancamentoDTO dto : dtosConfirmados) {
            Optional<ContaBancaria> contaOpt = (dto.getContaBancariaId() != null) ? contaBancariaRepository.findById(dto.getContaBancariaId()) : Optional.empty();
            Optional<Categoria> catOpt = (dto.getCategoriaId() != null) ? categoriaRepository.findById(dto.getCategoriaId()) : Optional.empty();

            String status = catOpt.isPresent() ? "MANUAL" : "PENDENTE";
            if ("AUTO".equalsIgnoreCase(dto.getStatusCategorizacao()) && catOpt.isPresent()) {
                status = "AUTO";
            }

            Lancamento entity = LancamentoBuilder.criar(
                    contaOpt.orElse(null),
                    catOpt.orElse(null),
                    dto.getDataLancamento(),
                    dto.getDescricao(),
                    dto.getValor(),
                    dto.getTipo(),
                    dto.getOrigem(),
                    dto.getCodigoAutorizacao(),
                    dto.getParcelaAtual(),
                    dto.getTotalParcelas(),
                    status,
                    dto.getObservacao()
            );

            Lancamento salvo = lancamentoRepository.save(entity);
            salvosDTO.add(LancamentoBuilder.paraDTO(salvo));
        }

        return salvosDTO;
    }
}
