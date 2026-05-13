import application.FitManager;
import mocks.DataMock;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;
import ui.screen.JOptionPaneUI;
import ui.screen.TerminalUI;

import javax.swing.*;

public class FitManagerApp {

    private static final boolean DEV_MODE = true;

    public static void main(String[] args) {

        // Permite ao usuário escolher a interface antes de iniciar o sistema
        UserInterface ui = selectUserInterface();
        if (ui == null) {
            return; // Usuário cancelou a seleção
        }

        FitManager fitManager = new FitManager();

        // Carrega dados de demonstração se DEV_MODE está ativo
        if (DEV_MODE) {
            DataMock.populateDemo(fitManager);
            ui.showMessage("Modo de Desenvolvimento ativado!\n\n" +
                    "O sistema foi carregado com dados de demonstração.\n" +
                    "Você pode explorar todas as funcionalidades sem inserir dados manualmente.\n\n" +
                    "Para desativar este modo, mude a variável DEV_MODE em FitManagerApp para false.");
        }

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }

    /**
     * Exibe uma tela de seleção para o usuário escolher qual
     * implementação de UserInterface deseja usar.
     *
     * Usa JOptionPane para a escolha inicial (antes de qualquer UI estar criada).
     * Retorna null se o usuário cancelar.
     *
     * @return instância da interface escolhida, ou null se cancelou
     */
    private static UserInterface selectUserInterface() {
        String[] options = {"Interface Gráfica (JOptionPane)", "Terminal (Linha de Comando)"};

        int choice = JOptionPane.showOptionDialog(
                null,
                "Bem-vindo ao FitManager!\n\nEscolha o modo de interface:",
                "FitManager > SELEÇÃO DE INTERFACE",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        return switch (choice) {
            case JOptionPane.OK_OPTION -> new JOptionPaneUI();
            case JOptionPane.NO_OPTION -> new TerminalUI();
            default -> null; // Usuário fechou o diálogo
        };
    }
}
