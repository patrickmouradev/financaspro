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
public class CategoriaDTO {

    private Long id;
    private String nome;
    private String icone;
    private String cor;
    private String tipo;

    @Builder.Default
    private List<RegraCategoriaDTO> regras = new ArrayList<>();
}
