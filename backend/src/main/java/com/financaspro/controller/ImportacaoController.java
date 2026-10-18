package com.financaspro.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.financaspro.model.dto.AtivoDetectadoDTO;
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
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/extrato/importar")
public class ImportacaoController {

    private final ImportacaoService importacaoService;
    private final ObjectMapper objectMapper;

    public ImportacaoController(ImportacaoService importacaoService, ObjectMapper objectMapper) {
        this.importacaoService = importacaoService;
        this.objectMapper = objectMapper;
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
    public ResponseEntity<List<LancamentoDTO>> confirmarImportacao(@RequestBody JsonNode body) throws Exception {
        List<LancamentoDTO> lancamentos = new ArrayList<>();
        List<AtivoDetectadoDTO> novosAtivos = new ArrayList<>();

        if (body.isArray()) {
            lancamentos = objectMapper.readerForListOf(LancamentoDTO.class).readValue(body);
        } else if (body.isObject()) {
            if (body.has("lancamentos") && body.get("lancamentos").isArray()) {
                lancamentos = objectMapper.readerForListOf(LancamentoDTO.class).readValue(body.get("lancamentos"));
            }
            if (body.has("novosAtivos") && body.get("novosAtivos").isArray()) {
                novosAtivos = objectMapper.readerForListOf(AtivoDetectadoDTO.class).readValue(body.get("novosAtivos"));
            }
        }

        if (!novosAtivos.isEmpty()) {
            importacaoService.cadastrarNovosAtivosSeNaoExistirem(novosAtivos);
        }

        List<LancamentoDTO> salvos = importacaoService.confirmarImportacao(lancamentos);
        return ResponseEntity.ok(salvos);
    }

    @PostMapping("/zerar-dados")
    public ResponseEntity<String> zerarDados() {
        importacaoService.zerarTodosOsDados();
        return ResponseEntity.ok("Todos os lançamentos, operações, proventos e ativos foram zerados com sucesso.");
    }
}
