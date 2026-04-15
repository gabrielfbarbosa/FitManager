package util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Utilitário responsável por formatar valores monetários no padrão brasileiro (BRL).
 *
 * Usa {@link NumberFormat} com {@link Locale} "pt-BR" para garantir:
 *  - ponto (.) como separador de milhar
 *  - vírgula (,) como separador decimal
 *  - duas casas decimais
 *
 * Exemplos:
 *  2596.5   -> "R$ 2.596,50"
 *  1090.0   -> "R$ 1.090,00"
 *  99.9     -> "R$ 99,90"
 */
public final class CurrencyFormatter {

    private static final Locale BRAZIL = new Locale("pt", "BR");

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(BRAZIL);

    private static final NumberFormat NUMBER_FORMAT;

    static {
        NUMBER_FORMAT = NumberFormat.getNumberInstance(BRAZIL);
        NUMBER_FORMAT.setMinimumFractionDigits(2);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    private CurrencyFormatter() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Formata um valor monetário usando o formato completo do Locale pt-BR.
     * Ex.: 2596.5 -> "R$ 2.596,50".
     */
    public static String formatCurrency(double value) {
        return CURRENCY_FORMAT.format(value);
    }
}
