package ui.menus.plan;

import ui.screen.UserInterface;

/**
 * Menu de gerenciamento de planos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class PlanMenu {

    private final UserInterface ui;

    public PlanMenu(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * Loop principal do menu de planos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (PlanMenuOption opt : PlanMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR PLANOS", menuOptions);

            if (input == null) { running = false; continue; }

            PlanMenuOption option = PlanMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + PlanMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case CADASTRAR:      running = false;   break;
                case CONSULTAR_NOME: running = false;   break;
                case ALTERAR_PRECO:  running = false;   break;
                case LISTAR:         running = false;   break;
                case VOLTAR:         running = false;   break;
            }
        }
    }
}