import application.FitManager;
import exceptions.PersistenceException;
import mocks.DataMock;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;
import ui.screen.JOptionPaneUI;
import ui.screen.TerminalUI;

import javax.swing.JOptionPane;

/**
 * Ponto de entrada do FitManager.
 *
 * Coordena o ciclo de vida da aplicação em quatro passos legíveis: escolha da
 * interface, carregamento dos dados persistidos, execução do menu principal e
 * gravação dos dados no encerramento. Cada passo é delegado a um método privado
 * com responsabilidade única, mantendo o {@link #main(String[])} curto e claro.
 */
public class FitManagerApp {

    /**
     * Modo de desenvolvimento. Quando {@code true}, popula o sistema com dados
     * de demonstração ({@link DataMock}) caso ele inicie vazio — útil para
     * explorar as funcionalidades sem cadastrar dados manualmente.
     *
     * Mantenha {@code false} para uso normal/avaliação: a primeira execução
     * inicia vazia, como esperado. Ative apenas durante o desenvolvimento.
     */
    private static final boolean DEV_MODE = false;

    private static final String MSG_DEV_MODE_ON =
            "Modo de Desenvolvimento ativado!\n\n"
            + "O sistema foi carregado com dados de demonstração "
            + "(nenhum arquivo de persistência foi encontrado).\n"
            + "Você pode explorar todas as funcionalidades sem inserir dados manualmente.\n\n"
            + "Para desativar, defina DEV_MODE como false em FitManagerApp.";

    private static final String MSG_EMPTY_START =
            "O sistema iniciou sem nenhum dado cadastrado.\n\n"
            + "Dica: existe um Modo de Desenvolvimento (constante DEV_MODE em FitManagerApp) "
            + "que popula automaticamente o sistema com dados de demonstração.\n"
            + "Defina DEV_MODE como true para ativá-lo.";

    public static void main(String[] args) {
        UserInterface ui = selectUserInterface();
        if (ui == null) {
            return; // usuário cancelou a seleção de interface
        }

        FitManager fitManager = new FitManager();

        loadData(fitManager, ui);
        prepareInitialData(fitManager, ui);

        new MainMenu(ui, fitManager).start();

        saveData(fitManager, ui);
    }

    /**
     * Exibe a tela de seleção para o usuário escolher a implementação de
     * {@link UserInterface}. Usa {@link JOptionPane} porque ocorre antes de
     * qualquer UI estar criada.
     *
     * @return a interface escolhida, ou {@code null} se o usuário cancelar
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
            default -> null; // usuário fechou o diálogo
        };
    }

    /**
     * Carrega os dados persistidos da sessão anterior. Arquivo ausente é
     * situação normal (primeira execução) e não gera erro; falhas de leitura
     * (corrupção, I/O) são comunicadas ao usuário sem encerrar abruptamente.
     */
    private static void loadData(FitManager fitManager, UserInterface ui) {
        try {
            fitManager.loadAll();
        } catch (PersistenceException e) {
            ui.showError("Falha ao carregar dados persistidos:\n" + e.getMessage()
                    + "\n\nO sistema iniciará com os dados que conseguiu carregar até o erro.");
        }
    }

    /**
     * Trata o início com o sistema vazio. Com {@code DEV_MODE} ativo, popula os
     * dados de demonstração; caso contrário, apenas informa que essa opção
     * existe. Se já houver dados persistidos, não faz nada.
     */
    private static void prepareInitialData(FitManager fitManager, UserInterface ui) {
        if (!fitManager.isEmpty()) {
            return; // já há dados de uma sessão anterior — não sobrescrever
        }
        if (DEV_MODE) {
            DataMock.populateDemo(fitManager);
            ui.showMessage(MSG_DEV_MODE_ON);
        } else {
            ui.showMessage(MSG_EMPTY_START);
        }
    }

    /**
     * Persiste todos os dados no encerramento. Falhas de escrita são informadas
     * ao usuário antes de encerrar, para que ele saiba que as alterações da
     * sessão podem não ter sido salvas.
     */
    private static void saveData(FitManager fitManager, UserInterface ui) {
        try {
            fitManager.saveAll();
        } catch (PersistenceException e) {
            ui.showError("Falha ao salvar dados no encerramento:\n" + e.getMessage()
                    + "\n\nAs alterações desta sessão podem ter sido perdidas.");
        }
    }
}
