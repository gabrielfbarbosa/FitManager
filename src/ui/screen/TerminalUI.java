package ui.screen;

import java.util.Scanner;

/**
 * Implementação de UserInterface via terminal (linha de comando).
 *
 * Estende {@link BaseUserInterface}, que concentra os loops de
 * validação compartilhados com {@code JOptionPaneUI}. Esta classe fornece
 * apenas as primitivas de I/O específicas do terminal:
 *  - {@link #getInput(String)} — leitura de texto via {@link Scanner}
 *  - {@link #readMenuChoice(String, String, int)} — leitura formatada com
 *    separador e título destacado para apresentação de menu
 *  - {@link #showMessage(String)}, {@link #showError(String)},
 *    {@link #showScrollableMessage(String)} — saídas via {@code System.out}
 *
 * Particularidades do terminal:
 *  - Não há botão Cancel. As primitivas {@code getInput} e
 *    {@code readMenuChoice} nunca retornam {@code null}; entrada vazia
 *    devolve string vazia, que o loop da {@link BaseUserInterface}
 *    trata como "campo obrigatório" e repete.
 *  - {@link #showError(String)} usa códigos ANSI para colorir a mensagem em
 *    vermelho. {@code System.err} foi evitado porque tem buffer separado de
 *    {@code System.out} — a ordem das mensagens fica confusa no terminal.
 */
public class TerminalUI extends BaseUserInterface {

    private static final String SEPARATOR = "════════════════════════════════════════";
    private static final String RED = "\u001B[31m";;
    private static final String YELLOW = "\u001B[33m";;
    private static final String RESET = "\u001B[0m";

    private Scanner scanner;

    public TerminalUI() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getInput(String prompt) {
        System.out.println();
        System.out.println(prompt);
        System.out.print("> ");
        return scanner.nextLine().trim();
    }

    @Override
    protected String readMenuChoice(String title, String options, int maxOption) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  FitManager " + title);
        System.out.println(SEPARATOR);
        System.out.println(options);
        System.out.print("Escolha uma opção (1-" + maxOption + "): ");
        return scanner.nextLine().trim();
    }

    @Override
    public void showMessage(String message) {
        System.out.println();
        System.out.println(message);
        System.out.println();
        pauseForRead();
    }

    /**
     * Utiliza código ANSI para alterar a cor do texto de erro.
     * Quando uso o {@code System.err}, a mensagem é exibida em posição
     * incorreta por conta de {@code .err} usar um buffer diferente de
     * {@code .out}, fazendo a mensagem aparecer em local errado e
     * deixando a visualização no terminal confusa.
     */
    @Override
    public void showError(String message) {
        System.out.println();
        System.out.println(RED + "[ERRO] " + message + RESET);
        System.out.println();
        pauseForRead();
    }

    @Override
    public void showScrollableMessage(String message) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println(message);
        System.out.println(SEPARATOR);
        System.out.println();
        pauseForRead();
    }

    /**
     * Pausa a execução para o usuário ler a mensagem antes de continuar.
     * Aguarda o usuário pressionar Enter.
     */
    private void pauseForRead() {
        System.out.print(YELLOW + "Pressione Enter para continuar... " + RESET);
        scanner.nextLine();
    }
}
