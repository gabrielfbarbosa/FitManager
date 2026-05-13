package ui.screen;

import javax.swing.*;
import java.awt.*;

/**
 * Implementação gráfica de UserInterface usando JOptionPane (Swing).
 *
 * Exibe diálogos modais para menus, entradas de texto, mensagens e erros.
 * Mantém o comportamento original do sistema antes da refatoração.
 */
public class JOptionPaneUI implements UserInterface {

    private static final String APP_TITLE = "FitManager";

    @Override
    public String showMenu(String title, String options) {
        return JOptionPane.showInputDialog(
                null,
                options + "\n\nEscolha uma opção:",
                APP_TITLE + " " + title,
                JOptionPane.QUESTION_MESSAGE
        );
    }

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
