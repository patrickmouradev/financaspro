package com.financaspro.controller;

import com.financaspro.model.dto.AtivoDTO;
import com.financaspro.model.enums.TipoAtivo;
import com.financaspro.service.InvestimentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ativos")
public class AtivoController {

    private final InvestimentoService investimentoService;

    public AtivoController(InvestimentoService investimentoService) {
        this.investimentoService = investimentoService;
    }

    @GetMapping
    public ResponseEntity<List<AtivoDTO>> listarAtivos(@RequestParam(required = false) TipoAtivo tipo) {
        List<AtivoDTO> ativos = investimentoService.listarAtivos(tipo);
        return ResponseEntity.ok(ativos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AtivoDTO> buscarPorId(@PathVariable Long id) {
        AtivoDTO ativo = investimentoService.buscarAtivoPorId(id);
        return ResponseEntity.ok(ativo);
    }

    @PostMapping
    public ResponseEntity<AtivoDTO> cadastrarAtivo(@Valid @RequestBody AtivoDTO dto) {
        AtivoDTO criado = investimentoService.cadastrarAtivo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AtivoDTO> atualizarAtivo(@PathVariable Long id, @Valid @RequestBody AtivoDTO dto) {
        AtivoDTO atualizado = investimentoService.atualizarAtivo(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarAtivo(@PathVariable Long id) {
        investimentoService.deletarAtivo(id);
        return ResponseEntity.noContent().build();
    }
}
