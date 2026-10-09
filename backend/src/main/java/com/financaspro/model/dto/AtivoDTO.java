package com.financaspro.model.dto;

import com.financaspro.model.enums.TipoAtivo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtivoDTO {

    private Long id;

    @NotBlank(message = "Ticker é obrigatório")
    private String ticker;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotNull(message = "Tipo do ativo é obrigatório")
    private TipoAtivo tipo;

    private String categoriaNome;
    private String classe;
    private String indexador;
    private BigDecimal taxaAdicional;
    private String setor;

    private LocalDate dataAplicacao;
    private LocalDate dataVencimento;
    private String liquidez;
    private String porcentagemTaxa;
    private String emissor;
    private String cnpjEmissor;
    private String produto;

    private OffsetDateTime criadoEm;
}
