import ui.menus.main.MainMenu;
import ui.screen.UserInterface;

public class FitManagerApp {

    public static void main(String[] args) {

        // Instancia os componentes principais
        UserInterface ui = new UserInterface();
        MainMenu mainMenu = new MainMenu(ui);

        // Inicia o sistema
        mainMenu.start();
    }
}