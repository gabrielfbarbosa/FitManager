package ui.menus.student;

import ui.screen.UserInterface;

/**
 * Menu de gerenciamento de alunos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class StudentMenu {

    private final UserInterface ui;

    public StudentMenu(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * Loop principal do menu de alunos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (StudentMenuOption opt : StudentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR ALUNOS", menuOptions);

            if (input == null) { running = false; continue; }

            StudentMenuOption option = StudentMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + StudentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case CADASTRAR:     running = false;    break;
                case CONSULTAR_CPF: running = false;    break;
                case EDITAR:        running = false;    break;
                case EXCLUIR:       running = false;    break;
                case LISTAR:        running = false;    break;
                case VOLTAR:        running = false;    break;
            }
        }
    }
}
