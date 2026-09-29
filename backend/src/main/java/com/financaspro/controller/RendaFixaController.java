package com.financaspro.controller;

import com.financaspro.model.dto.ComparacaoDTO;
import com.financaspro.model.dto.SimulacaoRequestDTO;
import com.financaspro.model.dto.SimulacaoResultadoDTO;
import com.financaspro.model.entity.SimulacaoSalva;
import com.financaspro.service.CalculadoraRendaFixaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/renda-fixa")
public class RendaFixaController {

    private final CalculadoraRendaFixaService calculadoraRendaFixaService;

    public RendaFixaController(CalculadoraRendaFixaService calculadoraRendaFixaService) {
        this.calculadoraRendaFixaService = calculadoraRendaFixaService;
    }

    @PostMapping("/simular")
    public ResponseEntity<SimulacaoResultadoDTO> simular(@Valid @RequestBody SimulacaoRequestDTO req) {
        SimulacaoResultadoDTO resultado = calculadoraRendaFixaService.simular(req);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/simular/salvar")
    public ResponseEntity<SimulacaoResultadoDTO> simularESalvar(@Valid @RequestBody SimulacaoRequestDTO req) {
        SimulacaoResultadoDTO salvo = calculadoraRendaFixaService.salvarSimulacao(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/simulacoes")
    public ResponseEntity<List<SimulacaoSalva>> listarSimulacoesSalvas() {
        List<SimulacaoSalva> lista = calculadoraRendaFixaService.listarSimulacoesSalvas();
        return ResponseEntity.ok(lista);
    }

    @PostMapping("/comparar")
    public ResponseEntity<ComparacaoDTO> comparar(@RequestBody Map<String, SimulacaoRequestDTO> payload) {
        SimulacaoRequestDTO req1 = payload.get("opcao1");
        SimulacaoRequestDTO req2 = payload.get("opcao2");

        if (req1 == null || req2 == null) {
            throw new IllegalArgumentException("Informe 'opcao1' e 'opcao2' no corpo da requisição para comparação.");
        }

        ComparacaoDTO comparacao = calculadoraRendaFixaService.comparar(req1, req2);
        return ResponseEntity.ok(comparacao);
    }
}
