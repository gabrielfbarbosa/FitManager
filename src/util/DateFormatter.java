package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitário responsável por formatar datas, horários e timestamps
 * no padrão brasileiro.
 *
 * Padrões suportados:
 *  - Data       : dd/MM/yyyy         (ex.: 14/04/2026)
 *  - Hora       : HH:mm:ss           (ex.: 09:30:45)
 *  - Data+Hora  : dd/MM/yyyy HH:mm:ss (ex.: 14/04/2026 09:30:45)
 *
 * Também expõe o formatter de data para operações de parse em entradas
 * digitadas pelo usuário, evitando a repetição de "dd/MM/yyyy" pelo código.
 */
public final class DateFormatter {

    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String DATE_TIME_PATTERN = DATE_PATTERN + " " + TIME_PATTERN;

    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.of("pt", "BR"));

    public static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, Locale.of("pt", "BR"));

    private DateFormatter() {
        // Classe utilitária
    }

    /**
     * Formata um {@link LocalDate} como "dd/MM/yyyy".
     * Retorna string vazia se o valor for nulo.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return DATE_FORMATTER.format(date);
    }

    public static String formatDateTime(LocalDateTime date) {
        if (date == null) return "";
        return DATE_TIME_FORMATTER.format(date);
    }

    /**
     * Converte uma string no padrão "dd/MM/yyyy" para {@link LocalDate}.
     * Lança {@link java.time.format.DateTimeParseException} em entradas inválidas.
     */
    public static LocalDate parseDate(String value) {
        return LocalDate.parse(value.trim(), DATE_FORMATTER);
    }

    /**
     * Converte uma string no padrão "dd/MM/yyyy HH:mm:ss" para {@link LocalDateTime}.
     * Utilizado pela camada de persistência ao reconstruir pagamentos a partir
     * de arquivo.
     * Lança {@link java.time.format.DateTimeParseException} em entradas inválidas.
     */
    public static LocalDateTime parseDateTime(String value) {
        return LocalDateTime.parse(value.trim(), DATE_TIME_FORMATTER);
    }
}
