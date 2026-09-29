package com.financaspro.service;

import com.financaspro.model.entity.IndicadorEconomico;
import com.financaspro.repository.IndicadorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class IpcaService {

    private static final Logger log = LoggerFactory.getLogger(IpcaService.class);

    private static final String BCB_URL_ULTIMO = "https://api.bcb.gov.br/dados/serie/bcdata.sgs.{serie}/dados/ultimos/12?formato=json";
    private static final int SERIE_IPCA = 10844;
    private static final int SERIE_CDI = 432; // SELIC Meta a.a. como referência de taxa de juros

    private final IndicadorRepository indicadorRepository;
    private final RestTemplate restTemplate;

    public IpcaService(IndicadorRepository indicadorRepository,
                       RestTemplateBuilder restTemplateBuilder) {
        this.indicadorRepository = indicadorRepository;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(4))
                .setReadTimeout(Duration.ofSeconds(4))
                .build();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void sincronizarIndicadoresBCB() {
        log.info("Iniciando sincronização de indicadores econômicos junto ao BCB...");
        sincronizarSerie("IPCA", SERIE_IPCA);
        sincronizarSerie("CDI", SERIE_CDI);
    }

    @SuppressWarnings("unchecked")
    private void sincronizarSerie(String tipo, int codigoSerie) {
        try {
            List<Map<String, String>> dados = restTemplate.getForObject(BCB_URL_ULTIMO, List.class, codigoSerie);
            if (dados != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                for (Map<String, String> item : dados) {
                    String dataStr = item.get("data");
                    String valorStr = item.get("valor");

                    if (dataStr != null && valorStr != null) {
                        LocalDate data = LocalDate.parse(dataStr, formatter);
                        String anoMes = String.format("%04d-%02d", data.getYear(), data.getMonthValue());
                        BigDecimal valor = new BigDecimal(valorStr);

                        Optional<IndicadorEconomico> existente = indicadorRepository.findByTipoAndAnoMes(tipo, anoMes);
                        if (existente.isEmpty()) {
                            IndicadorEconomico ind = IndicadorEconomico.builder()
                                    .tipo(tipo)
                                    .anoMes(anoMes)
                                    .valorPercentual(valor)
                                    .fonte("BCB_SGS")
                                    .build();
                            indicadorRepository.save(ind);
                        }
                    }
                }
                log.info("Sincronização de {} (Série {}) concluída com sucesso.", tipo, codigoSerie);
            }
        } catch (Exception e) {
            log.warn("Erro ao buscar indicador {} da Série BCB {}: {}", tipo, codigoSerie, e.getMessage());
        }
    }

    public BigDecimal obterUltimaTaxaOuPadrao(String tipo, BigDecimal padraoFallback) {
        Optional<IndicadorEconomico> ind = indicadorRepository.findTop1ByTipoOrderByAnoMesDesc(tipo);
        return ind.map(IndicadorEconomico::getValorPercentual).orElse(padraoFallback);
    }

    public List<IndicadorEconomico> listarIndicadoresPorTipo(String tipo) {
        return indicadorRepository.findByTipoOrderByAnoMesDesc(tipo);
    }
}
