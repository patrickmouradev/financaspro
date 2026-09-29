package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportacaoResultadoDTO {

    private int totalLidos;
    private int categorizadosAutomaticos;
    private int pendentesCategorizacao;
    private int ignoradosDeduplicacao;

    @Builder.Default
    private List<LancamentoDTO> lancamentos = new ArrayList<>();

    @Builder.Default
    private List<String> alertas = new ArrayList<>();
}
