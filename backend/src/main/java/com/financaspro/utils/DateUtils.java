package com.financaspro.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public final class DateUtils {

    public static final DateTimeFormatter FORMATO_DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter FORMATO_DATA_HORA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter FORMATO_ANO_MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private DateUtils() {
        // Construtor privado para classe utilitária
    }

    public static String anoMesAtual() {
        YearMonth anoMes = YearMonth.now();
        return anoMes.format(FORMATO_ANO_MES);
    }

    public static String formatarDataBr(LocalDate data) {
        if (data == null) {
            return "";
        }
        return data.format(FORMATO_DATA_BR);
    }

    public static String formatarDataHoraBr(LocalDateTime dataHora) {
        if (dataHora == null) {
            return "";
        }
        return dataHora.format(FORMATO_DATA_HORA_BR);
    }

    public static LocalDate primeiroDiaMes(YearMonth anoMes) {
        if (anoMes == null) {
            return LocalDate.now().withDayOfMonth(1);
        }
        return anoMes.atDay(1);
    }

    public static LocalDate ultimoDiaMes(YearMonth anoMes) {
        if (anoMes == null) {
            return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }
        return anoMes.atEndOfMonth();
    }

    public static long calcularDiasEntre(LocalDate inicio, LocalDate fim) {
        if (inicio == null || fim == null) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(inicio, fim);
    }
}
