import application.FitManager;
import exceptions.PersistenceException;
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

        // Carrega dados persistidos da sessão anterior (se houver).
        // Falhas de leitura (arquivo corrompido, IO) são comunicadas ao usuário
        // sem encerrar abruptamente. Arquivo ausente é situação normal e silenciosa.
        try {
            fitManager.loadAll();
        } catch (PersistenceException e) {
            ui.showError("Falha ao carregar dados persistidos:\n" + e.getMessage()
                    + "\n\nO sistema iniciará com os dados que conseguiu carregar até o erro.");
        }

        // Popula com dados de demonstração apenas se DEV_MODE está ativo
        // e o sistema iniciou vazio (sem arquivos de persistência ou
        // arquivos vazios). Assim, o DataMock não sobrescreve dados reais
        // de uma sessão anterior.
        if (DEV_MODE && fitManager.isEmpty()) {
            DataMock.populateDemo(fitManager);
            ui.showMessage("Modo de Desenvolvimento ativado!\n\n" +
                    "O sistema foi carregado com dados de demonstração (nenhum arquivo de persistência foi encontrado).\n" +
                    "Você pode explorar todas as funcionalidades sem inserir dados manualmente.\n\n" +
                    "Para desativar este modo, mude a variável DEV_MODE em FitManagerApp para false.");
        }

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();

        // Ao encerrar, persiste todos os dados em arquivo.
        // Falhas de escrita são informadas ao usuário antes do encerramento
        // para que ele saiba que os dados da sessão podem não ter sido salvos.
        try {
            fitManager.saveAll();
        } catch (PersistenceException e) {
            ui.showError("Falha ao salvar dados no encerramento:\n" + e.getMessage()
                    + "\n\nAs alterações desta sessão podem ter sido perdidas.");
        }
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
