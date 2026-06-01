package ui.screen;

import javax.swing.*;
import java.awt.*;

/**
 * Implementação gráfica de UserInterface usando JOptionPane (Swing).
 *
 * Estende {@link BaseUserInterface}, que concentra os loops de
 * validação compartilhados com {@code TerminalUI}. Esta classe fornece
 * apenas as primitivas de I/O específicas do Swing:
 *  - {@link #getInput(String)} — diálogo de entrada de texto
 *  - {@link #readMenuChoice(String, String, int)} — diálogo formatado para menu
 *  - {@link #showMessage(String)}, {@link #showError(String)},
 *    {@link #showScrollableMessage(String)} — diálogos de exibição
 *
 * Semântica de Cancel: {@code JOptionPane.showInputDialog} devolve
 * {@code null} quando o usuário clica em Cancel ou fecha o diálogo, e a
 * própria string vazia {@code ""} quando o usuário clica OK sem digitar
 * nada. Essa diferença é o que permite à {@link BaseUserInterface}
 * sair do loop de validação ao cancelar e repetir ao receber vazio.
 */
public class JOptionPaneUI extends BaseUserInterface {

    private static final String APP_TITLE = "FitManager";

    @Override
    public String getInput(String prompt) {
        return JOptionPane.showInputDialog(
                null,
                prompt,
                APP_TITLE,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    @Override
    protected String readMenuChoice(String title, String options, int maxOption) {
        return JOptionPane.showInputDialog(
                null,
                options + "\nEscolha uma opção (1-" + maxOption + "):",
                APP_TITLE + " " + title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

    @Override
    public void showMessage(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE,
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    @Override
    public void showError(String message) {
        JOptionPane.showMessageDialog(
                null,
                message,
                APP_TITLE + " | [ERRO]",
                JOptionPane.ERROR_MESSAGE
        );
    }

    @Override
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
}
