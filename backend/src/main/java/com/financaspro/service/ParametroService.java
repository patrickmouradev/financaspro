package com.financaspro.service;

import com.financaspro.builder.ParametroBuilder;
import com.financaspro.model.dto.ParametroDTO;
import com.financaspro.model.entity.Parametro;
import com.financaspro.repository.ParametroRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParametroService {

    private final ParametroRepository parametroRepository;

    public ParametroService(ParametroRepository parametroRepository) {
        this.parametroRepository = parametroRepository;
    }

    public List<ParametroDTO> listarTodos() {
        List<Parametro> lista = parametroRepository.findAll();
        return lista.stream()
                .map(ParametroBuilder::paraDTO)
                .collect(Collectors.toList());
    }

    public ParametroDTO buscarPorChaveDTO(String chave) {
        Parametro parametro = buscarEntidadePorChave(chave);
        return ParametroBuilder.paraDTO(parametro);
    }

    public String buscarValorPorChave(String chave, String valorPadrao) {
        Optional<Parametro> opt = parametroRepository.findByChave(chave);
        if (opt.isPresent() && opt.get().getValor() != null && !opt.get().getValor().trim().isEmpty()) {
            return opt.get().getValor().trim();
        }
        return valorPadrao;
    }

    public ParametroDTO atualizar(String chave, String novoValor) {
        Parametro parametro = buscarEntidadePorChave(chave);
        parametro.setValor(novoValor);
        Parametro salvo = parametroRepository.save(parametro);
        return ParametroBuilder.paraDTO(salvo);
    }

    private Parametro buscarEntidadePorChave(String chave) {
        return parametroRepository.findByChave(chave)
                .orElseThrow(() -> new RuntimeException("Parâmetro não encontrado: " + chave));
    }
}
