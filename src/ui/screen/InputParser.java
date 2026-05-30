package ui.screen;

import exceptions.InvalidFormatFieldException;
import exceptions.RequiredFieldException;

/**
 * Utilitário estático para validação e conversão de entradas do usuário.
 *
 * Centraliza a lógica de parsing que é usada pelos menus para validar
 * e converter entradas do usuário antes de processá-las.
 *
 * Política de Etapa 3:
 *  - Os métodos {@link #parseInt(String, String)} e {@link #parseDouble(String, String)}
 *    capturam {@link NumberFormatException} internamente e relançam como exceções
 *    da hierarquia do FitManager — {@link RequiredFieldException} para entrada vazia
 *    ou {@link InvalidFormatFieldException} para formato inválido. Assim, qualquer
 *    falha de conversão sobe pela cadeia até o {@code catch (FitManagerException e)}
 *    do menu, sem precisar de checagens de valor sentinela espalhadas.
 *  - O método {@link #isNumeric(String)} permanece como uma checagem booleana,
 *    usada apenas para validar opções de seleção em menus (onde o "não é número"
 *    não é uma falha de domínio, e sim uma decisão de fluxo).
 */
public final class InputParser {

    private InputParser() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Verifica se uma string representa um número inteiro válido.
     * Não lança exceção — retorna apenas {@code true} ou {@code false}.
     *
     * Uso típico: validação de opção numérica em menus, onde uma entrada
     * inválida apenas exibe a mensagem "Opção inválida" e repete o menu.
     *
     * @param value string a verificar (pode conter espaços nas bordas)
     * @return {@code true} se {@code value} pode ser convertido para int
     */
    public static boolean isNumeric(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            Integer.parseInt(value.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Converte uma string em inteiro.
     *
     * @param value     string a converter
     * @param fieldName nome do campo (usado na mensagem da exceção)
     * @return valor inteiro convertido
     * @throws RequiredFieldException       se {@code value} for nulo ou vazio
     * @throws InvalidFormatFieldException  se {@code value} não representar um inteiro válido
     */
    public static int parseInt(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldException(fieldName);
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidFormatFieldException(fieldName, "Número inteiro");
        }
    }

    /**
     * Converte uma string em double.
     * Aceita vírgula como separador decimal (padrão pt-BR) e ponto.
     *
     * @param value     string a converter
     * @param fieldName nome do campo (usado na mensagem da exceção)
     * @return valor decimal convertido
     * @throws RequiredFieldException       se {@code value} for nulo ou vazio
     * @throws InvalidFormatFieldException  se {@code value} não representar um decimal válido
     */
    public static double parseDouble(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldException(fieldName);
        }
        String normalized = value.trim().replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            throw new InvalidFormatFieldException(fieldName, "Número decimal (ex.: 99,90)");
        }
    }
}
