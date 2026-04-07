package ui.menus.enrollment;

import ui.screen.UserInterface;

/**
 * Menu de gerenciamento de matrículas.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class EnrollmentMenu {

    private final UserInterface ui;

    public EnrollmentMenu(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * Loop principal do menu de matrículas.
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (EnrollmentMenuOption opt : EnrollmentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR MATRÍCULAS", menuOptions);

            if (input == null) { running = false; continue; }

            EnrollmentMenuOption option = EnrollmentMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a "
                        + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case REALIZAR_MATRICULA:   running = false; break;
                case REGISTRAR_PAGAMENTO:  running = false; break;
                case CANCELAR_MATRICULA:   running = false; break;
                case CONSULTAR_ATIVA:      running = false; break;
                case LISTAR_HISTORICO:     running = false; break;
                case VOLTAR:               running = false; break;
            }
        }
    }
}