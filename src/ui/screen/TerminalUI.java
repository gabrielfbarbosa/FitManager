package ui.screen;

import java.util.Scanner;

/**
 * Implementação de UserInterface via terminal (linha de comando).
 *
 * Usa Scanner para leitura e System.out para saída.
 *
 * Em showMenu(), Enter vazio retorna "" (entrada inválida, exibe erro).
 * O usuário sai dos menus pela opção "Voltar" ou "Sair".
 *
 * Em getInput(), Enter vazio retorna null (cancela a operação atual),
 * equivalente ao botão "Cancelar" do JOptionPaneUI.
 */
public class TerminalUI implements UserInterface {

    private static final String SEPARATOR = "════════════════════════════════════════";

    private Scanner scanner;

    public TerminalUI() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String showMenu(String title, String options) {
        System.out.println();
        System.out.println(SEPARATOR);
        System.out.println("  FitManager " + title);
        System.out.println(SEPARATOR);
        System.out.println(options);
        System.out.print("Escolha uma opção: ");

        return scanner.nextLine().trim();
    }

    @Override
    public String getInput(String prompt) {
        System.out.println();
        System.out.println(prompt);
        System.out.print("> ");

        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return null;
        }
        return input;
    }

    @Override
    public void showMessage(String message) {
        System.out.println();
        System.out.println(message);
        System.out.println();
        pauseForRead();
    }

    @Override
    public void showError(String message) {
        System.out.println();
        System.out.println("[ERRO] " + message);
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
        System.out.print("Pressione Enter para continuar...");
        scanner.nextLine();
    }
}
