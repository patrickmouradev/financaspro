package com.financaspro.controller;

import com.financaspro.model.dto.DividendoDTO;
import com.financaspro.modulo.fundos.service.FiiService;
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
@RequestMapping("/api/dividendos")
public class DividendoController {

    private final FiiService fiiService;

    public DividendoController(FiiService fiiService) {
        this.fiiService = fiiService;
    }

    @GetMapping
    public ResponseEntity<List<DividendoDTO>> listarTodos() {
        List<DividendoDTO> dividendos = fiiService.listarTodosDividendos();
        return ResponseEntity.ok(dividendos);
    }

    @GetMapping("/fii/{fiiId}")
    public ResponseEntity<List<DividendoDTO>> listarPorFii(@PathVariable Long fiiId) {
        List<DividendoDTO> dividendos = fiiService.listarDividendosPorFii(fiiId);
        return ResponseEntity.ok(dividendos);
    }

    @PostMapping
    public ResponseEntity<DividendoDTO> registrarDividendo(@Valid @RequestBody DividendoDTO dto) {
        DividendoDTO criado = fiiService.registrarDividendo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarDividendo(@PathVariable Long id) {
        fiiService.deletarDividendo(id);
        return ResponseEntity.noContent().build();
    }
}
