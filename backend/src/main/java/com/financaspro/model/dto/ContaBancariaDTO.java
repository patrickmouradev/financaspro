package com.financaspro.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContaBancariaDTO {

    private Long id;
    private String nome;
    private String banco;
    private String tipo;
    private String agencia;
    private String numeroConta;
    private String finalCartao;
    private Boolean ativo;
}
