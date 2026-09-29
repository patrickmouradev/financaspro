package com.financaspro.scheduler;

import com.financaspro.service.IpcaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IpcaScheduler {

    private static final Logger log = LoggerFactory.getLogger(IpcaScheduler.class);

    private final IpcaService ipcaService;

    public IpcaScheduler(IpcaService ipcaService) {
        this.ipcaService = ipcaService;
    }

    // Executa no dia 15 de cada mês às 04:00 da manhã (quando o IBGE costuma divulgar o IPCA do mês anterior)
    @Scheduled(cron = "0 0 4 15 * ?")
    public void sincronizarIndicadoresMensais() {
        log.info("Sincronização agendada de indicadores econômicos iniciada...");
        try {
            ipcaService.sincronizarIndicadoresBCB();
        } catch (Exception e) {
            log.error("Erro durante sincronização agendada dos indicadores: {}", e.getMessage(), e);
        }
    }
}
