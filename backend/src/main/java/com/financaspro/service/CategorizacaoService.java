package com.financaspro.service;

import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.RegraCategoria;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.RegraCategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CategorizacaoService {

    private static final Pattern PATTERN_FII_TICKER = Pattern.compile("\\b[A-Z]{4}11[B]?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_ACAO_TICKER = Pattern.compile("\\b[A-Z]{4}[3456]\\b", Pattern.CASE_INSENSITIVE);

    private final RegraCategoriaRepository regraCategoriaRepository;
    private final CategoriaRepository categoriaRepository;

    public CategorizacaoService(RegraCategoriaRepository regraCategoriaRepository, CategoriaRepository categoriaRepository) {
        this.regraCategoriaRepository = regraCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public void categorizarLancamento(Lancamento lancamento) {
        if (lancamento == null || lancamento.getDescricao() == null) {
            return;
        }

        String desc = lancamento.getDescricao().toUpperCase().trim();
        List<RegraCategoria> regras = regraCategoriaRepository.findAllOrderByPrioridadeEPalavraChaveLength();

        for (RegraCategoria regra : regras) {
            String palavraChave = regra.getPalavraChave().toUpperCase().trim();
            if (!palavraChave.isEmpty() && desc.contains(palavraChave)) {
                lancamento.setCategoria(regra.getCategoria());
                lancamento.setStatusCategorizacao("AUTO");
                return;
            }
        }

        // 1. Categorização Automática por Especialista em Mercado Financeiro (Padrões de Ativos/Operações)
        if (categorizarPorMercadoFinanceiro(lancamento, desc)) {
            return;
        }

        // 2. Fallback por observação (ex: Categoria BTG original no extrato)
        if (lancamento.getObservacao() != null && lancamento.getObservacao().contains("Categoria BTG:")) {
            String catBtgStr = extrairCategoriaBtgDaObservacao(lancamento.getObservacao());
            if (catBtgStr != null) {
                Optional<Categoria> catOpt = categoriaRepository.findByNomeIgnoreCase(catBtgStr);
                if (catOpt.isPresent()) {
                    lancamento.setCategoria(catOpt.get());
                    lancamento.setStatusCategorizacao("AUTO");
                    return;
                }
            }
        }

        // Caso não encontre regra, atribui categoria 'Outros' se existir
        Optional<Categoria> catOutrosOpt = categoriaRepository.findByNomeIgnoreCase("Outros");
        catOutrosOpt.ifPresent(lancamento::setCategoria);
        lancamento.setStatusCategorizacao("PENDENTE");
    }

    private boolean categorizarPorMercadoFinanceiro(Lancamento lancamento, String desc) {
        boolean ehInvestimento = "NOTA_CORRETAGEM".equalsIgnoreCase(lancamento.getOrigem()) ||
                "EXTRATO_INVESTIMENTO".equalsIgnoreCase(lancamento.getOrigem()) ||
                "COMPRA_ATIVO".equalsIgnoreCase(lancamento.getTipo()) ||
                "VENDA_ATIVO".equalsIgnoreCase(lancamento.getTipo()) ||
                "DIVIDENDO".equalsIgnoreCase(lancamento.getTipo()) ||
                "RENDIMENTO".equalsIgnoreCase(lancamento.getTipo());

        // 1. Dividendos & Proventos
        if (desc.contains("DIVIDENDO") || desc.contains("RENDIMENTO") || desc.contains("PROVENTO") || desc.contains("JCP") || desc.contains("JUROS S/ CAPITAL")) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Dividendos & Proventos");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        // 2. Fundos Imobiliários (FIIs) - Regex Ticker Ex: XPML11, BTLG11, MXRF11, VISC11 ou palavras FII
        if (desc.contains("FII") || desc.contains("FUNDO IMOBILIARIO") || PATTERN_FII_TICKER.matcher(desc).find()) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Fundos Imobiliários");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        // 3. Ações - Regex Ticker Ex: PETR4, VALE3, WEGE3, ITUB4 ou palavras AÇÕES
        if (desc.contains("AÇÃO") || desc.contains("ACOES") || PATTERN_ACAO_TICKER.matcher(desc).find()) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Ações");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        // 4. Renda Fixa
        if (desc.contains("CDB") || desc.contains("TESOURO") || desc.contains("LCI") || desc.contains("LCA") || desc.contains("CRI") || desc.contains("CRA") || desc.contains("DEBENTURE")) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Renda Fixa");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        // 5. Fundos de Investimento
        if (desc.contains("FIM") || desc.contains("FIA") || desc.contains("FIC") || desc.contains("MULTIMERCADO")) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Fundos de Investimento");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        // 6. Fallback para Categoria Geral 'Investimentos' se for origem de Nota/Extrato Investimento
        if (ehInvestimento) {
            Optional<Categoria> cat = categoriaRepository.findByNomeIgnoreCase("Investimentos");
            if (cat.isPresent()) {
                lancamento.setCategoria(cat.get());
                lancamento.setStatusCategorizacao("AUTO");
                return true;
            }
        }

        return false;
    }

    private String extrairCategoriaBtgDaObservacao(String obs) {
        try {
            int idx = obs.indexOf("Categoria BTG:");
            if (idx != -1) {
                String sub = obs.substring(idx + "Categoria BTG:".length()).trim();
                int idxPipe = sub.indexOf("|");
                if (idxPipe != -1) {
                    return sub.substring(0, idxPipe).trim();
                }
                return sub.trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
