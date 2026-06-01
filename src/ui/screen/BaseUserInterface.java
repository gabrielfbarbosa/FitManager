package ui.screen;

import util.DateFormatter;
import util.UserInputParser;

import java.time.LocalDate;


/**
 * Implementação abstrata compartilhada de {@link UserInterface}.
 *
 * Concentra os loops de validação que eram duplicados entre
 * {@code JOptionPaneUI} e {@code TerminalUI} — todos os getters tipados
 * ({@link #getInput(String, String)}, {@link #getInt}, {@link #getDouble},
 * {@link #getDate}, {@link #showMenu}, seguem o mesmo
 * padrão: ler entrada bruta, validar, exibir erro e repetir até receber
 * valor válido ou cancelamento.
 *
 * As subclasses concretas precisam fornecer apenas as primitivas de I/O,
 * que variam entre a interface gráfica (Swing) e a de terminal:
 *  - {@link #getInput(String)} — leitura de texto bruto
 *  - {@link #readMenuChoice(String, String, int)} — leitura específica de
 *    opção de menu (com formatação visual diferente em cada UI)
 *  - {@link #showMessage(String)}, {@link #showError(String)},
 *    {@link #showScrollableMessage(String)} — exibição de saída
 *
 * Semântica de retorno {@code null}: as primitivas devem retornar
 * {@code null} apenas para "cancelar" (botão Cancel no JOptionPane). Entrada
 * vazia válida deve devolver string vazia, NÃO {@code null} — o loop trata
 * vazio como "campo obrigatório" e repete a solicitação. Como o terminal
 * não tem Cancel, sua implementação nunca retorna {@code null}.
 */
public abstract class BaseUserInterface implements UserInterface {

    // ============================
    // Primitivas — implementadas pelas UIs concretas
    // ============================

    /**
     * Lê a opção do menu em forma bruta (string ou {@code null} para Cancel).
     * Cada UI formata o prompt do menu à sua maneira (separadores no terminal,
     * cabeçalho no JOptionPane).
     */
    protected abstract String readMenuChoice(String title, String options, int maxOption);

    // ============================
    // Implementações compartilhadas
    // ============================

    /**
     * Exibe o menu e repete até receber uma opção válida no intervalo
     * {@code 1..maxOption}. Vazio nunca encerra navegação; só Cancel
     * (primitivo retorna {@code null}) retorna {@code null}.
     */
    @Override
    public Integer showMenu(String title, String options, int maxOption) {
        String errorMessage = "Opção inválida. Digite um número de 1 a " + maxOption + ".";
        while (true) {

            String raw = readMenuChoice(title, options, maxOption);

            if (raw == null) return null; // Cancel

            try {
                int choice = Integer.parseInt(raw.trim());
                if (choice >= 1 && choice <= maxOption) return choice;
            } catch (NumberFormatException e) {
                // Silenciar o erro, para exibir o erro na tela, mantendo o loop, para aguardar um valor valido
            }
            showError(errorMessage);
        }
    }

    /**
     * Captura uma entrada obrigatória, repetindo enquanto a entrada for
     * nula ou em branco. Cancel retorna {@code null}. A validação aqui
     * evita que o valor vazio chegue ao service.
     */
    @Override
    public String getInput(String prompt, String fieldName) {
        return getRequiredInput(prompt, fieldName);
    }

    /**
     * Captura um inteiro, repetindo até receber um valor parseável.
     * Cancel retorna {@code null}.
     */
    @Override
    public Integer getInt(String prompt, String fieldName) {
        return readAndParse(
                prompt,
                fieldName,
                Integer::parseInt,
                "Formato esperado: Número inteiro"
        );
    }

    /**
     * Captura um decimal, repetindo até receber um valor parseável.
     * Aceita vírgula como separador decimal (padrão pt-BR).
     * Cancel retorna {@code null}.
     */
    @Override
    public Double getDouble(String prompt, String fieldName) {
        return readAndParse(
                prompt,
                fieldName,
                value -> Double.parseDouble(value.replace(",", ".")),
                "Formato esperado: Número decimal ex: 32,99"
        );
    }

    /**
     * Captura uma data no padrão {@code dd/MM/yyyy}, repetindo até
     * receber um valor válido. Cancel retorna {@code null}.
     */
    @Override
    public LocalDate getDate(String prompt, String fieldName) {
        return readAndParse(
                prompt,
                fieldName,
                DateFormatter::parseDate,
                "Formato esperado: " + DateFormatter.DATE_PATTERN + " (ex.: 30/07/1993)"
        );
    }

    /**
     * Captura uma entrada obrigatória, repetindo enquanto o valor estiver
     * vazio. Retorna {@code null} em caso de cancelamento.
     */
    protected String getRequiredInput(String prompt, String fieldName) {
        while (true) {
            String raw = getInput(prompt);

            if (raw == null) {
                return null;
            }

            if (raw.isBlank()) {
                showError("O campo \"" + fieldName + "\" é obrigatório.");
                continue;
            }

            return raw.trim();
        }
    }

    /**
     * Captura uma entrada obrigatória e aplica o parser informado.
     * Em caso de erro de conversão, exibe uma mensagem e repete a solicitação.
     *
     * @param parser estratégia de conversão da entrada
     * @param formatMessage formato esperado exibido em caso de erro
     * @param <T> tipo retornado pelo parser
     * @return valor convertido ou {@code null} em caso de cancelamento
     */
    protected <T> T readAndParse(
            String prompt,
            String fieldName,
            UserInputParser<T> parser,
            String formatMessage
    ) {
        while (true) {

            String raw = getRequiredInput(prompt, fieldName);

            if (raw == null) {
                return null;
            }

            try {
                return parser.parse(raw);
            } catch (Exception e) {
                showError("O campo \"" + fieldName + "\" está em formato inválido. " + formatMessage);
            }
        }
    }
}
