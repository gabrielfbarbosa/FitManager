package ui.menus.main;

import ui.menus.plan.PlanMenu;
import ui.menus.reports.ReportsMenu;
import ui.menus.student.StudentMenu;
import ui.screen.UserInterface;

import ui.menus.enrollment.EnrollmentMenu;

/**
 * Menu principal do sistema FitManager.
 *
 * Responsável por exibir o menu principal e direcionar o usuário
 * para os submenus específicos de cada funcionalidade.
 *
 * Utiliza Lazy Instantiation: os submenus são criados sob demanda
 * na primeira vez que o usuário acessa a opção correspondente,
 * e reutilizados nas chamadas seguintes. Isso evita criar objetos
 * desnecessários e mantém referência única ao UserInterface e FitManager.
 */
public class MainMenu {

    private final UserInterface ui;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserInterface ui) {
        this.ui = ui;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui);
        }
        return reportsMenu;
    }

    /**
     * Inicia o loop principal do sistema.
     * O sistema permanece em execução até que a opção "Sair" seja escolhida.
     */
    public void start() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (MainMenuOption opt : MainMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getOptionName() + "\n";
            }
            String input = ui.showMenu("", menuOptions);

            if (input == null) {
                running = false;
                continue;
            }

            MainMenuOption option = MainMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + MainMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                case RELATORIOS:           getReportsMenu().run();    break;
                case SAIR:                 running = false;           break;
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo. 👋");
    }
}
