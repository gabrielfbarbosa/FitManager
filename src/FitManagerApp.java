import application.FitManager;
import ui.menus.main.MainMenu;
import ui.screen.UserInterface;

public class FitManagerApp {

    public static void main(String[] args) {

        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        FitManager fitManager = new FitManager();

        MainMenu mainMenu = new MainMenu(ui, fitManager);

        // Inicia o sistema
        mainMenu.start();
    }
}