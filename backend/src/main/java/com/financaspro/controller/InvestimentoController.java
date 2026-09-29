package com.financaspro.controller;

import com.financaspro.model.dto.CarteiraDTO;
import com.financaspro.model.dto.RentabilidadeDTO;
import com.financaspro.model.entity.Ativo;
import com.financaspro.repository.AtivoRepository;
import com.financaspro.service.RentabilidadeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/investimentos")
public class InvestimentoController {

    private final RentabilidadeService rentabilidadeService;
    private final AtivoRepository ativoRepository;

    public InvestimentoController(RentabilidadeService rentabilidadeService,
                                  AtivoRepository ativoRepository) {
        this.rentabilidadeService = rentabilidadeService;
        this.ativoRepository = ativoRepository;
    }

    @GetMapping("/carteira")
    public ResponseEntity<CarteiraDTO> obterCarteira() {
        CarteiraDTO carteira = rentabilidadeService.calcularCarteira();
        return ResponseEntity.ok(carteira);
    }

    @GetMapping("/rentabilidade/{ticker}")
    public ResponseEntity<RentabilidadeDTO> obterRentabilidadeTicker(@PathVariable String ticker) {
        Ativo ativo = ativoRepository.findByTicker(ticker.toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Ativo não encontrado com ticker: " + ticker));

        RentabilidadeDTO rentabilidade = rentabilidadeService.calcularRentabilidadeAtivo(ativo);
        if (rentabilidade == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(rentabilidade);
    }
}
