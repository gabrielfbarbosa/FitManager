import ui.screen.UserInterface;

import javax.swing.*;

public class FitManagerApp {

    public static void main(String[] args) {

        // Instancia os componentes principais
        UserInterface ui = new UserInterface();

        JOptionPane.showInputDialog(
                null,
                "Escolha uma opção:",
                "Tela incial",
                JOptionPane.QUESTION_MESSAGE
        );
    }
}