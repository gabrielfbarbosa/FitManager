package ui.menus.reports;

import ui.screen.UserInterface;

/**
 * Menu de relatórios e listagens.
 *
 * Consolida informações disponíveis nos serviços e as exibe de formas
 * diferentes conforme o critério solicitado.
 *
 * A lógica de filtragem reside nos serviços — este menu apenas
 * solicita os dados ao FitManager e os formata para exibição.
 */
public class ReportsMenu {

    private final UserInterface ui;

    public ReportsMenu(UserInterface ui) {
        this.ui = ui;
    }

    /**
     * Loop principal do menu de relatórios.
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (ReportsMenuOption opt : ReportsMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> RELATÓRIOS", menuOptions);

            if (input == null) { running = false; continue; }

            ReportsMenuOption option = ReportsMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a "
                        + ReportsMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case ALUNOS_ATIVOS:    running = false; break;
                case SALDO_PENDENTE:   running = false; break;
                case TODAS_MATRICULAS: running = false; break;
                case VOLTAR:           running = false; break;
            }
        }
    }
}