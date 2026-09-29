package com.financaspro.controller;

import com.financaspro.model.dto.LancamentoDTO;
import com.financaspro.model.dto.ResumoMensalDTO;
import com.financaspro.service.ExtratoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/extrato")
public class ExtratoController {

    private final ExtratoService extratoService;

    public ExtratoController(ExtratoService extratoService) {
        this.extratoService = extratoService;
    }

    @GetMapping("/lancamentos")
    public ResponseEntity<List<LancamentoDTO>> listarLancamentos(@RequestParam(value = "anoMes", required = false) String anoMesStr) {
        YearMonth ym = parseAnoMes(anoMesStr);
        List<LancamentoDTO> lancamentos = extratoService.listarLancamentosPorMes(ym);
        return ResponseEntity.ok(lancamentos);
    }

    @PutMapping("/lancamentos/{id}")
    public ResponseEntity<LancamentoDTO> atualizarLancamento(@PathVariable Long id, @RequestBody LancamentoDTO dto) {
        LancamentoDTO atualizado = extratoService.atualizar(id, dto);
        return ResponseEntity.ok(atualizado);
    }

    @DeleteMapping("/lancamentos/{id}")
    public ResponseEntity<Void> deletarLancamento(@PathVariable Long id) {
        extratoService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/resumo-mensal")
    public ResponseEntity<ResumoMensalDTO> resumoMensal(@RequestParam(value = "anoMes", required = false) String anoMesStr) {
        YearMonth ym = parseAnoMes(anoMesStr);
        ResumoMensalDTO resumo = extratoService.resumoMensal(ym);
        return ResponseEntity.ok(resumo);
    }

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<byte[]> exportarPdf(@RequestParam(value = "anoMes", required = false) String anoMesStr) throws Exception {
        YearMonth ym = parseAnoMes(anoMesStr);
        byte[] pdfBytes = extratoService.exportarPdf(ym);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "extrato-" + ym.toString() + ".pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/relatorio/excel")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(value = "anoMes", required = false) String anoMesStr) throws Exception {
        YearMonth ym = parseAnoMes(anoMesStr);
        byte[] excelBytes = extratoService.exportarExcel(ym);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "extrato-" + ym.toString() + ".xlsx");

        return ResponseEntity.ok().headers(headers).body(excelBytes);
    }

    private YearMonth parseAnoMes(String anoMesStr) {
        if (anoMesStr != null && !anoMesStr.trim().isEmpty()) {
            try {
                return YearMonth.parse(anoMesStr.trim());
            } catch (Exception ignored) {
            }
        }
        return YearMonth.now();
    }
}
