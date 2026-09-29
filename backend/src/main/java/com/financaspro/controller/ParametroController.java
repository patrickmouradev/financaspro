package com.financaspro.controller;

import com.financaspro.model.dto.ParametroDTO;
import com.financaspro.service.ParametroService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ParametroService parametroService;

    public ParametroController(ParametroService parametroService) {
        this.parametroService = parametroService;
    }

    @GetMapping
    public ResponseEntity<List<ParametroDTO>> listarTodos() {
        List<ParametroDTO> lista = parametroService.listarTodos();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{chave}")
    public ResponseEntity<ParametroDTO> buscarPorChave(@PathVariable String chave) {
        ParametroDTO dto = parametroService.buscarPorChaveDTO(chave);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{chave}")
    public ResponseEntity<ParametroDTO> atualizar(@PathVariable String chave, @RequestBody Map<String, String> body) {
        String novoValor = body.get("valor");
        ParametroDTO dto = parametroService.atualizar(chave, novoValor);
        return ResponseEntity.ok(dto);
    }
}
