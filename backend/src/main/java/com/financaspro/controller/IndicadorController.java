package com.financaspro.controller;

import com.financaspro.model.entity.IndicadorEconomico;
import com.financaspro.service.IpcaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorController {

    private final IpcaService ipcaService;

    public IndicadorController(IpcaService ipcaService) {
        this.ipcaService = ipcaService;
    }

    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar() {
        ipcaService.sincronizarIndicadoresBCB();
        return ResponseEntity.ok("Sincronização de indicadores acionada com sucesso.");
    }

    @GetMapping("/{tipo}")
    public ResponseEntity<List<IndicadorEconomico>> listarPorTipo(@PathVariable String tipo) {
        List<IndicadorEconomico> indicadores = ipcaService.listarIndicadoresPorTipo(tipo.toUpperCase());
        return ResponseEntity.ok(indicadores);
    }
}
