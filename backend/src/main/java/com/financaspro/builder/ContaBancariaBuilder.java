package com.financaspro.builder;

import com.financaspro.model.dto.ContaBancariaDTO;
import com.financaspro.model.entity.ContaBancaria;

public final class ContaBancariaBuilder {

    private ContaBancariaBuilder() {
        // Construtor privado
    }

    public static ContaBancaria criar(String nome, String banco, String tipo, String agencia, String numeroConta, String finalCartao) {
        return ContaBancaria.builder()
                .nome(nome)
                .banco(banco)
                .tipo(tipo)
                .agencia(agencia)
                .numeroConta(numeroConta)
                .finalCartao(finalCartao)
                .ativo(true)
                .build();
    }

    public static ContaBancariaDTO paraDTO(ContaBancaria entity) {
        if (entity == null) {
            return null;
        }
        return ContaBancariaDTO.builder()
                .id(entity.getId())
                .nome(entity.getNome())
                .banco(entity.getBanco())
                .tipo(entity.getTipo())
                .agencia(entity.getAgencia())
                .numeroConta(entity.getNumeroConta())
                .finalCartao(entity.getFinalCartao())
                .ativo(entity.getAtivo())
                .build();
    }
}
