package com.financaspro.service;

import com.financaspro.builder.LancamentoBuilder;
import com.financaspro.model.dto.ImportacaoResultadoDTO;
import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.model.entity.Ativo;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.ContaBancaria;
import com.financaspro.model.entity.Fii;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.Operacao;
import com.financaspro.model.enums.TipoAtivo;
import com.financaspro.model.enums.TipoOperacao;
import com.financaspro.repository.AtivoRepository;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.ContaBancariaRepository;
import com.financaspro.repository.DividendoRepository;
import com.financaspro.repository.FiiRepository;
import com.financaspro.repository.LancamentoRepository;
import com.financaspro.repository.OperacaoRepository;
import com.financaspro.modulo.contacorrente.parser.BtgExtratoParser;
import com.financaspro.modulo.containvestimento.parser.BtgExtratoInvestimentoParser;
import com.financaspro.modulo.containvestimento.parser.BtgExtratoInvestimentoParser.ItemExtratoInvestimento;
import com.financaspro.modulo.faturacartao.parser.BtgFaturaParser;
import com.financaspro.modulo.fundos.parser.BtgNotaCorretagemParser;
import com.financaspro.modulo.fundos.parser.BtgNotaCorretagemParser.NotaCorretagemDTO;
import com.financaspro.modulo.fundos.parser.BtgNotaCorretagemParser.ItemNotaDTO;
import com.financaspro.modulo.fundos.service.DividendoReconciliacaoService;
import com.financaspro.modulo.rendafixa.parser.BtgRendaFixaParser;
import com.financaspro.modulo.rendafixa.parser.BtgRendaFixaParser.RendaFixaNotaDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ImportacaoService {

    private static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final BtgFaturaParser btgFaturaParser;
    private final BtgExtratoParser btgExtratoParser;
    private final BtgExtratoInvestimentoParser btgExtratoInvestimentoParser;
    private final BtgNotaCorretagemParser btgNotaCorretagemParser;
    private final BtgRendaFixaParser btgRendaFixaParser;
    private final DividendoReconciliacaoService dividendoReconciliacaoService;
    private final CategorizacaoService categorizacaoService;
    private final ParametroService parametroService;
    private final LancamentoRepository lancamentoRepository;
    private final ContaBancariaRepository contaBancariaRepository;
    private final CategoriaRepository categoriaRepository;
    private final FiiRepository fiiRepository;
    private final AtivoRepository ativoRepository;
    private final OperacaoRepository operacaoRepository;
    private final DividendoRepository dividendoRepository;

    public ImportacaoService(
            BtgFaturaParser btgFaturaParser,
            BtgExtratoParser btgExtratoParser,
            BtgExtratoInvestimentoParser btgExtratoInvestimentoParser,
            BtgNotaCorretagemParser btgNotaCorretagemParser,
            BtgRendaFixaParser btgRendaFixaParser,
            DividendoReconciliacaoService dividendoReconciliacaoService,
            CategorizacaoService categorizacaoService,
            ParametroService parametroService,
            LancamentoRepository lancamentoRepository,
            ContaBancariaRepository contaBancariaRepository,
            CategoriaRepository categoriaRepository,
            FiiRepository fiiRepository,
            AtivoRepository ativoRepository,
            OperacaoRepository operacaoRepository,
            DividendoRepository dividendoRepository) {
        this.btgFaturaParser = btgFaturaParser;
        this.btgExtratoParser = btgExtratoParser;
        this.btgExtratoInvestimentoParser = btgExtratoInvestimentoParser;
        this.btgNotaCorretagemParser = btgNotaCorretagemParser;
        this.btgRendaFixaParser = btgRendaFixaParser;
        this.dividendoReconciliacaoService = dividendoReconciliacaoService;
        this.categorizacaoService = categorizacaoService;
        this.parametroService = parametroService;
        this.lancamentoRepository = lancamentoRepository;
        this.contaBancariaRepository = contaBancariaRepository;
        this.categoriaRepository = categoriaRepository;
        this.fiiRepository = fiiRepository;
        this.ativoRepository = ativoRepository;
        this.operacaoRepository = operacaoRepository;
        this.dividendoRepository = dividendoRepository;
    }

    public ImportacaoResultadoDTO processarArquivo(InputStream inputStream, String nomeArquivo, Long contaBancariaId, String senhaFornecida) throws Exception {
        return processarArquivoComTipo(inputStream, nomeArquivo, "AUTO", contaBancariaId, senhaFornecida);
    }

    public ImportacaoResultadoDTO processarArquivoComTipo(InputStream inputStream, String nomeArquivo, String tipoArquivo, Long contaBancariaId, String senhaFornecida) throws Exception {
        byte[] bytes = inputStream.readAllBytes();
        List<Lancamento> lancamentosBrutos = new ArrayList<>();
        Map<Lancamento, RendaFixaNotaDTO> mapaRendaFixa = new HashMap<>();

        String senhaFinal = (senhaFornecida != null && !senhaFornecida.trim().isEmpty()) ? senhaFornecida.trim() : "";
        if (senhaFinal.isEmpty()) {
            senhaFinal = parametroService.buscarValorPorChave("senha_fatura_btg", "").trim();
        }
        if (senhaFinal.isEmpty()) {
            senhaFinal = parametroService.buscarValorPorChave("senha_arquivo_default", "").trim();
        }

        String tipoUpper = (tipoArquivo != null) ? tipoArquivo.toUpperCase() : "AUTO";
        String nomeUpper = (nomeArquivo != null) ? nomeArquivo.toUpperCase() : "";

        try {
            if ("COMPROVANTE_RENDA_FIXA".equalsIgnoreCase(tipoUpper) || "NOTA_CORRETAGEM_RENDA_FIXA".equalsIgnoreCase(tipoUpper)) {
                RendaFixaNotaDTO rf = btgRendaFixaParser.parsearComprovanteRendaFixa(bytes, senhaFinal);
                if (rf != null) {
                    String dataVencStr = (rf.getDataVencimento() != null) ? rf.getDataVencimento().format(FORMATO_DATA_BR) : "Nulo";
                    String desc = "APLICAÇÃO " + (rf.getProduto() != null ? rf.getProduto() : "RENDA FIXA")
                            + " - " + (rf.getEmissor() != null ? rf.getEmissor() : "BTG PACTUAL")
                            + " (Venc: " + dataVencStr + ")";

                    Lancamento l = Lancamento.builder()
                            .dataLancamento(rf.getDataAplicacao() != null ? rf.getDataAplicacao().atStartOfDay() : java.time.LocalDateTime.now())
                            .descricao(desc)
                            .valor(rf.getValorAplicacao() != null ? rf.getValorAplicacao().negate() : BigDecimal.ZERO)
                            .tipo("COMPRA_RENDA_FIXA")
                            .origem(tipoUpper)
                            .observacao("Produto: " + rf.getProduto() + " | Índice: " + rf.getIndice() + " | Taxa: " + rf.getTaxa() + " | Liquidez: " + rf.getLiquidez())
                            .statusCategorizacao("PENDENTE")
                            .build();

                    lancamentosBrutos.add(l);
                    mapaRendaFixa.put(l, rf);
                }
            } else if ("EXTRATO_INVESTIMENTO".equalsIgnoreCase(tipoUpper)) {
                List<ItemExtratoInvestimento> itensInv = btgExtratoInvestimentoParser.parsearExtratoInvestimento(bytes, senhaFinal);
                if (!itensInv.isEmpty()) {
                    dividendoReconciliacaoService.reconciliarEProcessarProventos(itensInv);
                    for (ItemExtratoInvestimento item : itensInv) {
                        lancamentosBrutos.add(Lancamento.builder()
                                .dataLancamento(item.getDataMovimento())
                                .descricao(item.getDescricao())
                                .valor(item.getValor())
                                .tipo(item.getTipo())
                                .origem(item.getOrigem())
                                .observacao(item.getObservacao())
                                .statusCategorizacao("PENDENTE")
                                .build());
                    }
                }
            } else if ("NOTA_CORRETAGEM".equalsIgnoreCase(tipoUpper) || nomeUpper.contains("NOTA") || nomeUpper.contains("CORRETAGEM")) {
                // Tenta primeiro Renda Fixa se tiver termos de Renda Fixa no arquivo
                if (btgRendaFixaParser.ehFormatoRendaFixa(bytes, nomeArquivo, senhaFinal)) {
                    RendaFixaNotaDTO rf = btgRendaFixaParser.parsearComprovanteRendaFixa(bytes, senhaFinal);
                    if (rf != null) {
                        String dataVencStr = (rf.getDataVencimento() != null) ? rf.getDataVencimento().format(FORMATO_DATA_BR) : "Nulo";
                        String desc = "APLICAÇÃO " + (rf.getProduto() != null ? rf.getProduto() : "RENDA FIXA")
                                + " - " + (rf.getEmissor() != null ? rf.getEmissor() : "BTG PACTUAL")
                                + " (Venc: " + dataVencStr + ")";

                        Lancamento l = Lancamento.builder()
                                .dataLancamento(rf.getDataAplicacao() != null ? rf.getDataAplicacao().atStartOfDay() : java.time.LocalDateTime.now())
                                .descricao(desc)
                                .valor(rf.getValorAplicacao() != null ? rf.getValorAplicacao().negate() : BigDecimal.ZERO)
                                .tipo("COMPRA_RENDA_FIXA")
                                .origem("NOTA_CORRETAGEM_RENDA_FIXA")
                                .observacao("Produto: " + rf.getProduto() + " | Índice: " + rf.getIndice() + " | Taxa: " + rf.getTaxa() + " | Liquidez: " + rf.getLiquidez())
                                .statusCategorizacao("PENDENTE")
                                .build();

                        lancamentosBrutos.add(l);
                        mapaRendaFixa.put(l, rf);
                    }
                }

                if (lancamentosBrutos.isEmpty()) {
                    NotaCorretagemDTO nota = btgNotaCorretagemParser.parsearNotaCorretagem(bytes, senhaFinal);
                    if (nota != null && nota.getItens() != null && !nota.getItens().isEmpty()) {
                        for (ItemNotaDTO item : nota.getItens()) {
                            lancamentosBrutos.add(Lancamento.builder()
                                    .dataLancamento(nota.getDataPregao().atStartOfDay())
                                    .descricao("COMPRA " + item.getTicker() + " (Qtd: " + item.getQuantidade() + " @ R$ " + item.getPrecoUnitario() + " + Taxas R$ " + item.getTaxasProrrateadas() + ")")
                                    .valor(item.getValorTotalOperacao().negate())
                                    .tipo("COMPRA_ATIVO")
                                    .origem("NOTA_CORRETAGEM")
                                    .observacao("Preço Médio Ajustado com Taxas B3: R$ " + item.getPrecoMedioAjustadoComTaxas())
                                    .statusCategorizacao("PENDENTE")
                                    .build());
                        }
                    }
                }
            } else if ("FATURA_CARTAO".equalsIgnoreCase(tipoUpper) || nomeUpper.contains("FATURA")) {
                lancamentosBrutos = btgFaturaParser.parsearFatura(bytes, senhaFinal);
            } else if ("EXTRATO_CONTA_CORRENTE".equalsIgnoreCase(tipoUpper)) {
                lancamentosBrutos = btgExtratoParser.parsearExtrato(bytes, senhaFinal);
            } else {
                // Modo AUTO: Renda Fixa -> Nota de Corretagem -> Extrato Investimento -> Extrato Corrente -> Fatura
                try {
                    if (btgRendaFixaParser.ehFormatoRendaFixa(bytes, nomeArquivo, senhaFinal)) {
                        RendaFixaNotaDTO rf = btgRendaFixaParser.parsearComprovanteRendaFixa(bytes, senhaFinal);
                        if (rf != null) {
                            String dataVencStr = (rf.getDataVencimento() != null) ? rf.getDataVencimento().format(FORMATO_DATA_BR) : "Nulo";
                            String desc = "APLICAÇÃO " + (rf.getProduto() != null ? rf.getProduto() : "RENDA FIXA")
                                    + " - " + (rf.getEmissor() != null ? rf.getEmissor() : "BTG PACTUAL")
                                    + " (Venc: " + dataVencStr + ")";

                            Lancamento l = Lancamento.builder()
                                    .dataLancamento(rf.getDataAplicacao() != null ? rf.getDataAplicacao().atStartOfDay() : java.time.LocalDateTime.now())
                                    .descricao(desc)
                                    .valor(rf.getValorAplicacao() != null ? rf.getValorAplicacao().negate() : BigDecimal.ZERO)
                                    .tipo("COMPRA_RENDA_FIXA")
                                    .origem("COMPROVANTE_RENDA_FIXA")
                                    .observacao("Produto: " + rf.getProduto() + " | Índice: " + rf.getIndice() + " | Taxa: " + rf.getTaxa() + " | Liquidez: " + rf.getLiquidez())
                                    .statusCategorizacao("PENDENTE")
                                    .build();

                            lancamentosBrutos.add(l);
                            mapaRendaFixa.put(l, rf);
                        }
                    }
                } catch (Exception ignored) {}

                if (lancamentosBrutos.isEmpty()) {
                    try {
                        NotaCorretagemDTO nota = btgNotaCorretagemParser.parsearNotaCorretagem(bytes, senhaFinal);
                        if (nota != null && nota.getItens() != null && !nota.getItens().isEmpty()) {
                            for (ItemNotaDTO item : nota.getItens()) {
                                lancamentosBrutos.add(Lancamento.builder()
                                        .dataLancamento(nota.getDataPregao().atStartOfDay())
                                        .descricao("COMPRA " + item.getTicker() + " (Qtd: " + item.getQuantidade() + " @ R$ " + item.getPrecoUnitario() + " + Taxas R$ " + item.getTaxasProrrateadas() + ")")
                                        .valor(item.getValorTotalOperacao().negate())
                                        .tipo("COMPRA_ATIVO")
                                        .origem("NOTA_CORRETAGEM")
                                        .observacao("Preço Médio Ajustado com Taxas B3: R$ " + item.getPrecoMedioAjustadoComTaxas())
                                        .statusCategorizacao("PENDENTE")
                                        .build());
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (lancamentosBrutos.isEmpty()) {
                    try {
                        List<ItemExtratoInvestimento> itensInv = btgExtratoInvestimentoParser.parsearExtratoInvestimento(bytes, senhaFinal);
                        if (!itensInv.isEmpty()) {
                            dividendoReconciliacaoService.reconciliarEProcessarProventos(itensInv);
                            for (ItemExtratoInvestimento item : itensInv) {
                                lancamentosBrutos.add(Lancamento.builder()
                                        .dataLancamento(item.getDataMovimento())
                                        .descricao(item.getDescricao())
                                        .valor(item.getValor())
                                        .tipo(item.getTipo())
                                        .origem(item.getOrigem())
                                        .observacao(item.getObservacao())
                                        .statusCategorizacao("PENDENTE")
                                        .build());
                            }
                        }
                    } catch (Exception ignored) {}
                }

                if (lancamentosBrutos.isEmpty()) {
                    try {
                        lancamentosBrutos = btgExtratoParser.parsearExtrato(bytes, senhaFinal);
                    } catch (Exception ignored) {}
                }

                if (lancamentosBrutos.isEmpty()) {
                    try {
                        lancamentosBrutos = btgFaturaParser.parsearFatura(bytes, senhaFinal);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            String msg = (e.getMessage() != null) ? e.getMessage().toLowerCase() : "";
            if (msg.contains("password") || msg.contains("encrypted") || msg.contains("senha") || msg.contains("protected")) {
                throw new IllegalArgumentException("O arquivo enviado está protegido por senha. Por favor, informe o CPF ou senha de abertura.");
            }
            throw e;
        }

        if (lancamentosBrutos.isEmpty()) {
            throw new IllegalArgumentException("Nenhum lançamento foi encontrado no arquivo. Verifique se o tipo de arquivo selecionado está correto e se a senha foi informada.");
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
            }

            boolean duplicado = false;
            Optional<Lancamento> dupOpt = lancamentoRepository.findByDescricaoAndDataLancamentoAndValor(
                    l.getDescricao(), l.getDataLancamento(), l.getValor());
            if (dupOpt.isPresent()) {
                duplicado = true;
            }

            if (duplicado) {
                ignoradosDeduplicacao++;
                continue;
            }

            categorizacaoService.categorizarLancamento(l);
            if ("AUTO".equalsIgnoreCase(l.getStatusCategorizacao())) {
                autoCat++;
            } else {
                pendentes++;
            }

            LancamentoDTO dto = LancamentoBuilder.paraDTO(l);
            RendaFixaNotaDTO rf = mapaRendaFixa.get(l);
            if (rf != null) {
                dto.setDataAplicacao(rf.getDataAplicacao());
                dto.setDataVencimento(rf.getDataVencimento());
                dto.setLiquidez(rf.getLiquidez());
                dto.setIndice(rf.getIndice());
                dto.setTaxa(rf.getTaxa());
                dto.setProduto(rf.getProduto());
                dto.setEmissor(rf.getEmissor());
                dto.setCnpjEmissor(rf.getCnpjEmissor());
            }

            dtosPreview.add(dto);
        }

        if (ignoradosDeduplicacao > 0) {
            alertas.add(ignoradosDeduplicacao + " lançamentos já existentes foram ignorados por deduplicação.");
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

            // Sincroniza posições de custódia em FIIs, Ações e Renda Fixa ao confirmar importação
            if ("COMPRA_RENDA_FIXA".equalsIgnoreCase(dto.getTipo()) || (dto.getOrigem() != null && dto.getOrigem().toUpperCase().contains("RENDA_FIXA"))) {
                sincronizarRendaFixa(salvo, dto);
            } else {
                sincronizarCustodiaEFiis(salvo);
            }

            salvosDTO.add(LancamentoBuilder.paraDTO(salvo));
        }

        return salvosDTO;
    }

    private void sincronizarRendaFixa(Lancamento l, LancamentoDTO dto) {
        if (l == null) return;

        String produto = (dto.getProduto() != null && !dto.getProduto().trim().isEmpty()) ? dto.getProduto().trim() : "CDB Pós-Fixado";
        String emissor = (dto.getEmissor() != null && !dto.getEmissor().trim().isEmpty()) ? dto.getEmissor().trim() : "Banco BTG Pactual";
        String cnpj = dto.getCnpjEmissor();
        String liquidez = dto.getLiquidez();
        String indice = dto.getIndice(); // CDI, IPCA, SELIC, PREFIXADO (ou null)
        String taxa = dto.getTaxa();     // 100.00% do CDI (ou null)
        java.time.LocalDate dataAplicacao = dto.getDataAplicacao() != null ? dto.getDataAplicacao() : l.getDataLancamento().toLocalDate();
        java.time.LocalDate dataVencimento = dto.getDataVencimento();

        // Gera ticker único para o título de Renda Fixa ex: CDB_BTG_210828
        String slugProd = produto.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (slugProd.length() > 6) slugProd = slugProd.substring(0, 6);
        String slugEmissor = emissor.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (slugEmissor.length() > 4) slugEmissor = slugEmissor.substring(0, 4);

        String sufixoVenc = (dataVencimento != null) ? dataVencimento.format(DateTimeFormatter.ofPattern("ddMMyy")) : "S_VENC";
        String tickerRendaFixa = slugProd + "_" + slugEmissor + "_" + sufixoVenc;

        String nomeAtivo = produto + " - " + emissor;

        Ativo ativo = ativoRepository.findByTicker(tickerRendaFixa).orElseGet(() -> {
            return ativoRepository.save(Ativo.builder()
                    .ticker(tickerRendaFixa)
                    .nome(nomeAtivo)
                    .tipo(TipoAtivo.RENDA_FIXA)
                    .categoriaNome("Renda Fixa")
                    .classe(produto)
                    .indexador(indice)
                    .porcentagemTaxa(taxa)
                    .liquidez(liquidez)
                    .dataAplicacao(dataAplicacao)
                    .dataVencimento(dataVencimento)
                    .emissor(emissor)
                    .cnpjEmissor(cnpj)
                    .produto(produto)
                    .build());
        });

        // Atualiza atributos se o ativo já existia
        ativo.setIndexador(indice);
        ativo.setPorcentagemTaxa(taxa);
        ativo.setLiquidez(liquidez);
        ativo.setDataAplicacao(dataAplicacao);
        ativo.setDataVencimento(dataVencimento);
        ativo.setEmissor(emissor);
        ativo.setCnpjEmissor(cnpj);
        ativo.setProduto(produto);
        ativoRepository.save(ativo);

        Operacao op = Operacao.builder()
                .ativo(ativo)
                .tipo(TipoOperacao.COMPRA)
                .dataOperacao(dataAplicacao)
                .quantidade(BigDecimal.ONE)
                .precoUnitario(l.getValor().abs())
                .taxas(BigDecimal.ZERO)
                .dataVencimento(dataVencimento)
                .liquidez(liquidez)
                .porcentagemTaxa(taxa)
                .observacao("Importado via Comprovante/Nota de Renda Fixa")
                .build();

        operacaoRepository.save(op);
    }

    private void sincronizarCustodiaEFiis(Lancamento l) {
        if (l == null || l.getDescricao() == null) return;
        String desc = l.getDescricao().toUpperCase();
        String origem = (l.getOrigem() != null) ? l.getOrigem().toUpperCase() : "";

        if (!origem.contains("NOTA") && !origem.contains("INVESTIMENTO") && !"COMPRA_ATIVO".equalsIgnoreCase(l.getTipo()) && !"VENDA_ATIVO".equalsIgnoreCase(l.getTipo())) {
            return;
        }

        // Extrai ticker ex: XPML11, BTLG11, PETR4, etc.
        Matcher mTicker = Pattern.compile("\\b([A-Z]{4}[0-9]{1,2}[B]?)\\b").matcher(desc);
        if (!mTicker.find()) return;

        String ticker = mTicker.group(1).toUpperCase().trim();

        // Extrai quantidade
        BigDecimal qtd = BigDecimal.ONE;
        Matcher mQtd = Pattern.compile("QTD:\\s*(\\d+(?:[.,]\\d+)?)", Pattern.CASE_INSENSITIVE).matcher(desc);
        if (mQtd.find()) {
            try {
                qtd = new BigDecimal(mQtd.group(1).replace(",", "."));
            } catch (Exception ignored) {}
        }

        BigDecimal precoMedioAjustado = l.getValor().abs();
        if (qtd.compareTo(BigDecimal.ZERO) > 0) {
            precoMedioAjustado = l.getValor().abs().divide(qtd, 4, RoundingMode.HALF_UP);
        }

        if (l.getObservacao() != null && l.getObservacao().contains("Preço Médio Ajustado com Taxas B3: R$ ")) {
            try {
                int idx = l.getObservacao().indexOf("Preço Médio Ajustado com Taxas B3: R$ ");
                String valStr = l.getObservacao().substring(idx + "Preço Médio Ajustado com Taxas B3: R$ ".length()).trim();
                precoMedioAjustado = new BigDecimal(valStr.replace(",", "."));
            } catch (Exception ignored) {}
        }

        // 1. Sincroniza Entidade FII se for um Fundo Imobiliário
        if (ticker.endsWith("11") || ticker.endsWith("11B") || desc.contains("FII")) {
            final String fTicker = ticker;
            final BigDecimal fQtd = qtd;
            final BigDecimal fPm = precoMedioAjustado;

            Fii fii = fiiRepository.findByTicker(fTicker).orElseGet(() -> {
                return Fii.builder()
                        .ticker(fTicker)
                        .nome("FII " + fTicker)
                        .segmento("Imobiliário")
                        .quantidadeCotas(BigDecimal.ZERO)
                        .precoMedio(BigDecimal.ZERO)
                        .build();
            });

            BigDecimal qtdAnterior = (fii.getQuantidadeCotas() != null) ? fii.getQuantidadeCotas() : BigDecimal.ZERO;
            BigDecimal pmAnterior = (fii.getPrecoMedio() != null) ? fii.getPrecoMedio() : BigDecimal.ZERO;

            BigDecimal qtdNovaTotal = qtdAnterior.add(fQtd);
            BigDecimal custoTotalAnterior = qtdAnterior.multiply(pmAnterior);
            BigDecimal custoTotalNovo = fQtd.multiply(fPm);
            BigDecimal pmNovoPonderado = (qtdNovaTotal.compareTo(BigDecimal.ZERO) > 0)
                    ? custoTotalAnterior.add(custoTotalNovo).divide(qtdNovaTotal, 4, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            fii.setQuantidadeCotas(qtdNovaTotal);
            fii.setPrecoMedio(pmNovoPonderado);
            fiiRepository.save(fii);
        }

        // 2. Sincroniza Entidade Ativo e Operação na Carteira Geral
        final String aTicker = ticker;
        TipoAtivo tipoAtivo = (ticker.endsWith("11") || desc.contains("FII")) ? TipoAtivo.FII : TipoAtivo.ACAO;
        Ativo ativo = ativoRepository.findByTicker(aTicker).orElseGet(() -> {
            return ativoRepository.save(Ativo.builder()
                    .ticker(aTicker)
                    .nome((tipoAtivo == TipoAtivo.FII ? "FII " : "Ação ") + aTicker)
                    .tipo(tipoAtivo)
                    .categoriaNome(tipoAtivo == TipoAtivo.FII ? "Fundos Imobiliários" : "Ações")
                    .classe(tipoAtivo == TipoAtivo.FII ? "Tijolo" : "Blue Chips")
                    .indexador(tipoAtivo == TipoAtivo.FII ? "IPCA" : "Ibovespa")
                    .build());
        });

        TipoOperacao tipoOpEnum = "VENDA_ATIVO".equalsIgnoreCase(l.getTipo()) ? TipoOperacao.VENDA : TipoOperacao.COMPRA;

        Operacao op = Operacao.builder()
                .ativo(ativo)
                .tipo(tipoOpEnum)
                .dataOperacao(l.getDataLancamento().toLocalDate())
                .quantidade(qtd)
                .precoUnitario(precoMedioAjustado)
                .taxas(BigDecimal.ZERO)
                .observacao("Importado via Nota de Corretagem / Extrato")
                .build();
        operacaoRepository.save(op);
    }

    @Transactional
    public void zerarTodosOsDados() {
        operacaoRepository.deleteAllInBatch();
        dividendoRepository.deleteAllInBatch();
        fiiRepository.deleteAllInBatch();
        ativoRepository.deleteAllInBatch();
        lancamentoRepository.deleteAllInBatch();
    }
}
