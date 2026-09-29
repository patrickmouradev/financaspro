package com.financaspro.service;

import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.Lancamento;
import com.financaspro.model.entity.RegraCategoria;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.RegraCategoriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategorizacaoService {

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

        // Tenta fallback por observação (ex: Categoria BTG original no extrato)
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
