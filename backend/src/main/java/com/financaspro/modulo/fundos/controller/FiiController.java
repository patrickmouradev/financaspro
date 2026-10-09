package com.financaspro.modulo.fundos.controller;

import com.financaspro.model.dto.FiiDTO;
import com.financaspro.model.dto.ProjecaoMetaDTO;
import com.financaspro.modulo.fundos.service.FiiService;
import com.financaspro.modulo.fundos.service.MetaDividendoService;
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

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/fiis")
public class FiiController {

    private final FiiService fiiService;
    private final MetaDividendoService metaDividendoService;

    public FiiController(FiiService fiiService, MetaDividendoService metaDividendoService) {
        this.fiiService = fiiService;
        this.metaDividendoService = metaDividendoService;
    }

    @GetMapping
    public ResponseEntity<List<FiiDTO>> listarFiis() {
        List<FiiDTO> lista = fiiService.listarFiis();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FiiDTO> buscarPorId(@PathVariable Long id) {
        FiiDTO fii = fiiService.buscarFiiPorId(id);
        return ResponseEntity.ok(fii);
    }

    @PostMapping
    public ResponseEntity<FiiDTO> cadastrarFii(@Valid @RequestBody FiiDTO dto) {
        FiiDTO criado = fiiService.cadastrarFii(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(criado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FiiDTO> atualizarFii(@PathVariable Long id, @Valid @RequestBody FiiDTO dto) {
        FiiDTO atualizado = fiiService.atualizarFii(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletarFii(@PathVariable Long id) {
        fiiService.deletarFii(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/meta/projecao")
    public ResponseEntity<ProjecaoMetaDTO> obterProjecaoMeta(@RequestParam(defaultValue = "1000.00") BigDecimal metaMensal) {
        ProjecaoMetaDTO projecao = metaDividendoService.calcularProgressoEMeta(metaMensal);
        return ResponseEntity.ok(projecao);
    }
}
