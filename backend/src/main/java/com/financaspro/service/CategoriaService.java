package com.financaspro.service;

import com.financaspro.builder.CategoriaBuilder;
import com.financaspro.builder.RegraCategoriaBuilder;
import com.financaspro.model.dto.CategoriaDTO;
import com.financaspro.model.dto.RegraCategoriaDTO;
import com.financaspro.model.entity.Categoria;
import com.financaspro.model.entity.RegraCategoria;
import com.financaspro.repository.CategoriaRepository;
import com.financaspro.repository.RegraCategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final RegraCategoriaRepository regraCategoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, RegraCategoriaRepository regraCategoriaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.regraCategoriaRepository = regraCategoriaRepository;
    }

    public List<CategoriaDTO> listarTodas() {
        List<Categoria> categorias = categoriaRepository.findAll();
        List<CategoriaDTO> resultado = new ArrayList<>();

        for (Categoria cat : categorias) {
            CategoriaDTO dto = CategoriaBuilder.paraDTO(cat);
            List<RegraCategoria> regras = regraCategoriaRepository.findByCategoriaId(cat.getId());
            List<RegraCategoriaDTO> regrasDTO = new ArrayList<>();
            for (RegraCategoria r : regras) {
                regrasDTO.add(RegraCategoriaBuilder.paraDTO(r));
            }
            dto.setRegras(regrasDTO);
            resultado.add(dto);
        }

        return resultado;
    }

    public List<RegraCategoriaDTO> listarTodasRegras() {
        List<RegraCategoria> regras = regraCategoriaRepository.findAllOrderByPrioridadeEPalavraChaveLength();
        List<RegraCategoriaDTO> resultado = new ArrayList<>();
        for (RegraCategoria r : regras) {
            resultado.add(RegraCategoriaBuilder.paraDTO(r));
        }
        return resultado;
    }

    @Transactional
    public CategoriaDTO criar(CategoriaDTO dto) {
        Categoria cat = CategoriaBuilder.criar(dto.getNome(), dto.getIcone(), dto.getCor(), dto.getTipo());
        Categoria salva = categoriaRepository.save(cat);
        return CategoriaBuilder.paraDTO(salva);
    }

    @Transactional
    public CategoriaDTO atualizar(Long id, CategoriaDTO dto) {
        Categoria cat = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + id));

        if (dto.getNome() != null) cat.setNome(dto.getNome());
        if (dto.getIcone() != null) cat.setIcone(dto.getIcone());
        if (dto.getCor() != null) cat.setCor(dto.getCor());
        if (dto.getTipo() != null) cat.setTipo(dto.getTipo());

        Categoria salva = categoriaRepository.save(cat);
        return CategoriaBuilder.paraDTO(salva);
    }

    @Transactional
    public RegraCategoriaDTO adicionarRegra(Long categoriaId, String palavraChave, Integer prioridade) {
        Categoria cat = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + categoriaId));

        RegraCategoria regra = RegraCategoriaBuilder.criar(cat, palavraChave, prioridade);
        RegraCategoria salva = regraCategoriaRepository.save(regra);
        return RegraCategoriaBuilder.paraDTO(salva);
    }

    @Transactional
    public void removerRegra(Long regraId) {
        regraCategoriaRepository.deleteById(regraId);
    }

    @Transactional
    public void deletar(Long id) {
        categoriaRepository.deleteById(id);
    }
}
