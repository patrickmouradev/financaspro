package com.financaspro.controller;

import com.financaspro.model.dto.OperacaoDTO;
import com.financaspro.modulo.containvestimento.service.InvestimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/operacoes")
public class OperacaoController {

    private final InvestimentoService investimentoService;

    public OperacaoController(InvestimentoService investimentoService) {
        this.investimentoService = investimentoService;
    }

    @GetMapping
    public ResponseEntity<List<OperacaoDTO>> listarTodasOperacoes() {
        List<OperacaoDTO> operacoes = investimentoService.listarTodasOperacoes();
        return ResponseEntity.ok(operacoes);
    }

    @GetMapping("/ativo/{ativoId}")
    public ResponseEntity<List<OperacaoDTO>> listarPorAtivo(@PathVariable Long ativoId) {
        List<OperacaoDTO> operacoes = investimentoService.listarOperacoesPorAtivo(ativoId);
        return ResponseEntity.ok(operacoes);
    }

    @PostMapping
    public ResponseEntity<OperacaoDTO> registrarOperacao(@Valid @RequestBody OperacaoDTO dto) {
        OperacaoDTO criada = investimentoService.registrarOperacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarOperacao(@PathVariable Long id) {
        investimentoService.deletarOperacao(id);
        return ResponseEntity.noContent().build();
    }
}
