import application.FitManager;
import mocks.DataMock;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;

public class FitManagerApp {

    private static final boolean DEV_MODE = true; // Alterar para true para carregar dados de demonstração

    public static void main(String[] args) {

        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        FitManager fitManager = new FitManager();

        // Carrega dados de demonstração se DEV_MODE está ativo
        if (DEV_MODE) {
            DataMock.populateDemo(fitManager);
            ui.showMessage("Modo de Desenvolvimento ativado!\n\n" +
                    "O sistema foi carregado com dados de demonstração.\n" +
                    "Você pode explorar todas as funcionalidades sem inserir dados manualmente.\n\n" +
                    "Para desativar este modo, mude a varável DEV_MODE em FitManagerApp para false.");
        }

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}