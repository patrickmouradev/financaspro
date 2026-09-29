package com.financaspro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class PercentualUtils {

    private PercentualUtils() {
        // Construtor privado para classe utilitária
    }

    public static BigDecimal calcularVariacao(BigDecimal valorInicial, BigDecimal valorFinal) {
        if (valorInicial == null || valorFinal == null || valorInicial.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal diferenca = valorFinal.subtract(valorInicial);
        return diferenca.divide(valorInicial, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcularPercentual(BigDecimal parte, BigDecimal total) {
        if (parte == null || total == null || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return parte.divide(total, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal arredondar6Casas(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(6, RoundingMode.HALF_UP);
        }
        return valor.setScale(6, RoundingMode.HALF_UP);
    }
}
