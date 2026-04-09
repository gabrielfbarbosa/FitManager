package ui.screen;

/**
 * Utilitário estático para validação e conversão de entradas do usuário.
 *
 * Centraliza a lógica de parsing que é usada tanto pela UserInterface
 * (em getIntInput e getDoubleInput) quanto diretamente pelos menus
 * (para validar a seleção de opções antes de chamar parseInt).
 *
 * Toda validação é feita caractere a caractere, sem regex e sem try/catch.
 * Responsabilidade única: transformar strings brutas de entrada em valores
 * primitivos seguros, ou sinalizar entrada inválida via valores sentinela
 * (Integer.MIN_VALUE para int, Double.NaN para double).
 */
public class InputParser {

    /**
     * Verifica se uma string representa um número inteiro não-negativo.
     * Percorre caractere a caractere — sem regex e sem try/catch.
     *
     * @param value string a verificar (pode ter espaços nas bordas)
     * @return true se contiver apenas dígitos e não for vazia
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se uma string representa um número decimal válido.
     * Aceita dígitos e no máximo um ponto como separador decimal.
     * Percorre caractere a caractere — sem regex e sem try/catch.
     *
     * @param value string já normalizada (vírgula já substituída por ponto)
     * @return true se for um decimal válido (ex: "99.90", "100", "0.5")
     */
    public static boolean isDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        int dotCount = 0;
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '.') {
                dotCount++;
                if (dotCount > 1) return false;
            } else if (c < '0' || c > '9') {
                return false;
            }
        }
        // Rejeita string que é só "." sem nenhum dígito
        return !trimmed.equals(".");
    }

    /**
     * Converte uma string em inteiro de forma segura.
     * Retorna Integer.MIN_VALUE se a string for nula, vazia ou não numérica.
     *
     * @param value string a converter
     * @return valor inteiro, ou Integer.MIN_VALUE se inválido
     */
    public static int parseIntSafe(String value) {
        if (!isNumeric(value)) {
            return Integer.MIN_VALUE;
        }
        return Integer.parseInt(value.trim());
    }

    /**
     * Converte uma string em double de forma segura.
     * Aceita vírgula como separador decimal.
     * Retorna Double.NaN se a string for nula, vazia ou não numérica.
     *
     * @param value string a converter
     * @return valor double, ou Double.NaN se inválido
     */
    public static double parseDoubleSafe(String value) {
        if (value == null) {
            return Double.NaN;
        }
        String normalized = value.trim().replace(",", ".");
        if (!isDecimal(normalized)) {
            return Double.NaN;
        }
        return Double.parseDouble(normalized);
    }
}