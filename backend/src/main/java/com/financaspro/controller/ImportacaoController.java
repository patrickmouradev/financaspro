package com.financaspro.controller;

import com.financaspro.model.dto.ImportacaoResultadoDTO;
import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.service.ImportacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api/extrato/importar")
public class ImportacaoController {

    private final ImportacaoService importacaoService;

    public ImportacaoController(ImportacaoService importacaoService) {
        this.importacaoService = importacaoService;
    }

    @PostMapping
    public ResponseEntity<ImportacaoResultadoDTO> uploadEPreview(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "tipoArquivo", required = false, defaultValue = "AUTO") String tipoArquivo,
            @RequestParam(value = "contaBancariaId", required = false) Long contaBancariaId,
            @RequestParam(value = "senha", required = false) String senha) throws Exception {

        String nomeOriginal = arquivo.getOriginalFilename();
        InputStream is = arquivo.getInputStream();
        ImportacaoResultadoDTO resultado = importacaoService.processarArquivoComTipo(is, nomeOriginal, tipoArquivo, contaBancariaId, senha);

        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/confirmar")
    public ResponseEntity<List<LancamentoDTO>> confirmarImportacao(@RequestBody List<LancamentoDTO> dtosConfirmados) {
        List<LancamentoDTO> salvos = importacaoService.confirmarImportacao(dtosConfirmados);
        return ResponseEntity.ok(salvos);
    }

    @PostMapping("/zerar-dados")
    public ResponseEntity<String> zerarDados() {
        importacaoService.zerarTodosOsDados();
        return ResponseEntity.ok("Todos os lançamentos, operações, proventos e ativos foram zerados com sucesso.");
    }
}

