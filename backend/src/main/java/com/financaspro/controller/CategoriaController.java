package com.financaspro.controller;

import com.financaspro.model.dto.CategoriaDTO;
import com.financaspro.model.dto.RegraCategoriaDTO;
import com.financaspro.service.CategoriaService;
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
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<CategoriaDTO>> listarTodas() {
        List<CategoriaDTO> lista = categoriaService.listarTodas();
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity<CategoriaDTO> criar(@RequestBody CategoriaDTO dto) {
        CategoriaDTO criada = categoriaService.criar(dto);
        return ResponseEntity.ok(criada);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoriaDTO> atualizar(@PathVariable Long id, @RequestBody CategoriaDTO dto) {
        CategoriaDTO atualizada = categoriaService.atualizar(id, dto);
        return ResponseEntity.ok(atualizada);
    }

    @PostMapping("/{id}/regras")
    public ResponseEntity<RegraCategoriaDTO> adicionarRegra(
            @PathVariable("id") Long categoriaId,
            @RequestParam("palavraChave") String palavraChave,
            @RequestParam(value = "prioridade", required = false, defaultValue = "1") Integer prioridade) {
        RegraCategoriaDTO regra = categoriaService.adicionarRegra(categoriaId, palavraChave, prioridade);
        return ResponseEntity.ok(regra);
    }

    @DeleteMapping("/regras/{regraId}")
    public ResponseEntity<Void> removerRegra(@PathVariable Long regraId) {
        categoriaService.removerRegra(regraId);
        return ResponseEntity.noContent().build();
    }
}
