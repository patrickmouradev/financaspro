package com.financaspro.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class MoedaUtils {

    private static final Locale LOCALE_BR = new Locale("pt", "BR");

    private MoedaUtils() {
        // Construtor privado para classe utilitária
    }

    public static String formatarReal(BigDecimal valor) {
        if (valor == null) {
            return "R$ 0,00";
        }
        NumberFormat nf = NumberFormat.getCurrencyInstance(LOCALE_BR);
        return nf.format(valor);
    }

    public static BigDecimal arredondar2Casas(BigDecimal valor) {
        if (valor == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal somaBigDecimal(BigDecimal a, BigDecimal b) {
        BigDecimal valA = (a != null) ? a : BigDecimal.ZERO;
        BigDecimal valB = (b != null) ? b : BigDecimal.ZERO;
        return valA.add(valB).setScale(2, RoundingMode.HALF_UP);
    }
}
