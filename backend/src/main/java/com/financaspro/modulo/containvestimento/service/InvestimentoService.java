package com.financaspro.modulo.containvestimento.service;

import com.financaspro.builder.AtivoBuilder;
import com.financaspro.builder.OperacaoBuilder;
import com.financaspro.model.dto.AtivoDTO;
import com.financaspro.model.dto.OperacaoDTO;
import com.financaspro.model.entity.Ativo;
import com.financaspro.model.entity.Operacao;
import com.financaspro.model.enums.TipoAtivo;
import com.financaspro.repository.AtivoRepository;
import com.financaspro.repository.OperacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestimentoService {

    private final AtivoRepository ativoRepository;
    private final OperacaoRepository operacaoRepository;

    public InvestimentoService(AtivoRepository ativoRepository,
                               OperacaoRepository operacaoRepository) {
        this.ativoRepository = ativoRepository;
        this.operacaoRepository = operacaoRepository;
    }

    @Transactional
    public AtivoDTO cadastrarAtivo(AtivoDTO dto) {
        String tickerUpper = dto.getTicker().trim().toUpperCase();
        if (ativoRepository.existsByTicker(tickerUpper)) {
            throw new IllegalArgumentException("Ativo com ticker " + tickerUpper + " já existe.");
        }

        Ativo ativo = AtivoBuilder.umAtivo()
                .comTicker(tickerUpper)
                .comNome(dto.getNome())
                .comTipo(dto.getTipo())
                .comCategoriaNome(dto.getCategoriaNome())
                .comClasse(dto.getClasse())
                .comIndexador(dto.getIndexador())
                .comTaxaAdicional(dto.getTaxaAdicional())
                .comSetor(dto.getSetor())
                .build();

        Ativo salvo = ativoRepository.save(ativo);
        return converterParaAtivoDTO(salvo);
    }

    @Transactional
    public AtivoDTO atualizarAtivo(Long id, AtivoDTO dto) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com id: " + id));

        ativo.setNome(dto.getNome());
        ativo.setTipo(dto.getTipo());
        ativo.setCategoriaNome(dto.getCategoriaNome());
        ativo.setClasse(dto.getClasse());
        ativo.setIndexador(dto.getIndexador());
        ativo.setTaxaAdicional(dto.getTaxaAdicional());
        ativo.setSetor(dto.getSetor());

        Ativo salvo = ativoRepository.save(ativo);
        return converterParaAtivoDTO(salvo);
    }

    public List<AtivoDTO> listarAtivos(TipoAtivo tipo) {
        List<Ativo> ativos;
        if (tipo != null) {
            ativos = ativoRepository.findByTipo(tipo);
        } else {
            ativos = ativoRepository.findAll();
        }
        return ativos.stream().map(this::converterParaAtivoDTO).collect(Collectors.toList());
    }

    public AtivoDTO buscarAtivoPorId(Long id) {
        Ativo ativo = ativoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com id: " + id));
        return converterParaAtivoDTO(ativo);
    }

    @Transactional
    public void deletarAtivo(Long id) {
        if (!ativoRepository.existsById(id)) {
            throw new IllegalArgumentException("Ativo não encontrado com id: " + id);
        }
        ativoRepository.deleteById(id);
    }

    @Transactional
    public OperacaoDTO registrarOperacao(OperacaoDTO dto) {
        Ativo ativo = ativoRepository.findById(dto.getAtivoId())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com id: " + dto.getAtivoId()));

        Operacao operacao = OperacaoBuilder.umaOperacao()
                .comAtivo(ativo)
                .comTipo(dto.getTipo())
                .comDataOperacao(dto.getDataOperacao())
                .comQuantidade(dto.getQuantidade())
                .comPrecoUnitario(dto.getPrecoUnitario())
                .comTaxas(dto.getTaxas() != null ? dto.getTaxas() : BigDecimal.ZERO)
                .comObservacao(dto.getObservacao())
                .build();

        Operacao salva = operacaoRepository.save(operacao);
        return converterParaOperacaoDTO(salva);
    }

    public List<OperacaoDTO> listarOperacoesPorAtivo(Long ativoId) {
        return operacaoRepository.findByAtivoIdOrderByDataOperacaoAsc(ativoId).stream()
                .map(this::converterParaOperacaoDTO)
                .collect(Collectors.toList());
    }

    public List<OperacaoDTO> listarTodasOperacoes() {
        return operacaoRepository.findAllByOrderByDataOperacaoDesc().stream()
                .map(this::converterParaOperacaoDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletarOperacao(Long id) {
        if (!operacaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Operação não encontrada com id: " + id);
        }
        operacaoRepository.deleteById(id);
    }

    public AtivoDTO converterParaAtivoDTO(Ativo ativo) {
        return AtivoDTO.builder()
                .id(ativo.getId())
                .ticker(ativo.getTicker())
                .nome(ativo.getNome())
                .tipo(ativo.getTipo())
                .categoriaNome(ativo.getCategoriaNome())
                .classe(ativo.getClasse())
                .indexador(ativo.getIndexador())
                .taxaAdicional(ativo.getTaxaAdicional())
                .setor(ativo.getSetor())
                .dataAplicacao(ativo.getDataAplicacao())
                .dataVencimento(ativo.getDataVencimento())
                .liquidez(ativo.getLiquidez())
                .porcentagemTaxa(ativo.getPorcentagemTaxa())
                .emissor(ativo.getEmissor())
                .cnpjEmissor(ativo.getCnpjEmissor())
                .produto(ativo.getProduto())
                .criadoEm(ativo.getCriadoEm())
                .build();
    }

    public OperacaoDTO converterParaOperacaoDTO(Operacao op) {
        BigDecimal taxas = op.getTaxas() != null ? op.getTaxas() : BigDecimal.ZERO;
        BigDecimal total = op.getQuantidade().multiply(op.getPrecoUnitario()).add(taxas);

        return OperacaoDTO.builder()
                .id(op.getId())
                .ativoId(op.getAtivo().getId())
                .tickerAtivo(op.getAtivo().getTicker())
                .nomeAtivo(op.getAtivo().getNome())
                .tipo(op.getTipo())
                .dataOperacao(op.getDataOperacao())
                .quantidade(op.getQuantidade())
                .precoUnitario(op.getPrecoUnitario())
                .taxas(taxas)
                .totalOperacao(total)
                .observacao(op.getObservacao())
                .criadoEm(op.getCriadoEm())
                .build();
    }
}
