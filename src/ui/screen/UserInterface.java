package ui.screen;


import javax.swing.*;
import java.awt.*;

/**
 * Centraliza todas as operações de entrada e saída do sistema.
 *
 * Em vez de cada menu interagir diretamente com JOptionPane,
 * essas chamadas ficam encapsuladas nos métodos: showMenu(),
 * getInput(), showMessage() e showError().
 *
 * Qualquer mudança na forma de exibição afeta apenas esta classe,
 * sem impactar o restante do sistema.
 */
public class UserInterface {

    private static final String APP_TITLE = "FitManager";

    /**
     * Exibe um menu com título e opções, retornando a opção escolhida pelo usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param title   título do menu
     * @param options texto completo com as opções numeradas
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String showMenu(String title, String options) {
        return JOptionPane.showInputDialog(
                null,
                options + "\n\nEscolha uma opção:",
                APP_TITLE + " " + title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    /**
     * Captura uma entrada de texto do usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param prompt texto do prompt exibido
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String getInput(String prompt) {
        String input = JOptionPane.showInputDialog(
                null,
                prompt,
                APP_TITLE,
                JOptionPane.QUESTION_MESSAGE
        );
        return input;
    }

    /**
     * Exibe uma mensagem de sucesso/informação.
     *
     * @param message texto da mensagem
     */
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Exibe uma mensagem de erro.
     *
     * @param message texto do erro
     */
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE + " | [ERRO]",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Exibe uma Tela onde pode ser feito o scroll.
     *
     * @param message texto da informação exibida
     */
    public void showScrollableMessage(String message) {
        JTextArea textArea = new JTextArea(message);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 300));

        JOptionPane.showMessageDialog(
                null,
                scrollPane,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }


    /**
     * Solicita e converte uma entrada inteira do usuário.
     * Delega a validação e conversão ao InputParser.
     * Retorna Integer.MIN_VALUE se o usuário cancelar ou digitar valor não numérico.
     *
     * @param prompt texto do prompt exibido
     * @return o valor inteiro, ou Integer.MIN_VALUE se cancelou ou inválido
     */
    public int getIntInput(String prompt) {
        String input = getInput(prompt);
        return InputParser.parseIntSafe(input);
    }

    /**
     * Solicita e converte uma entrada decimal do usuário.
     * Aceita vírgula como separador decimal.
     * Delega a validação e conversão ao InputParser.
     * Retorna Double.NaN se o usuário cancelar ou digitar valor não numérico.
     *
     * @param prompt texto do prompt exibido
     * @return o valor decimal, ou Double.NaN se cancelou ou inválido
     */
    public double getDoubleInput(String prompt) {
        String input = getInput(prompt);
        return InputParser.parseDoubleSafe(input);
    }
}