package ui.menus.main;

import application.FitManager;
import exceptions.FitManagerException;
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

    private UserInterface ui;
    private FitManager fitManager;

    // Submenus — lazy instantiation
    private StudentMenu studentMenu;
    private PlanMenu planMenu;
    private EnrollmentMenu enrollmentMenu;
    private ReportsMenu reportsMenu;

    public MainMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    // ========================
    // Lazy Getters dos Submenus
    // ========================

    private StudentMenu getStudentMenu() {
        if (studentMenu == null) {
            studentMenu = new StudentMenu(ui, fitManager);
        }
        return studentMenu;
    }

    private PlanMenu getPlanMenu() {
        if (planMenu == null) {
            planMenu = new PlanMenu(ui, fitManager);
        }
        return planMenu;
    }

    private EnrollmentMenu getEnrollmentMenu() {
        if (enrollmentMenu == null) {
            enrollmentMenu = new EnrollmentMenu(ui, fitManager);
        }
        return enrollmentMenu;
    }

    private ReportsMenu getReportsMenu() {
        if (reportsMenu == null) {
            reportsMenu = new ReportsMenu(ui, fitManager);
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
            try {
                StringBuilder menuOptions = new StringBuilder();
                for (MainMenuOption opt : MainMenuOption.values()) {
                    menuOptions.append(opt.getNumber()).append(" - ").append(opt.getOptionName()).append("\n");
                }
                Integer choice = ui.showMenu("", menuOptions.toString(), MainMenuOption.values().length);

                if (choice == null) {
                    running = false;
                    continue;
                }

                MainMenuOption option = MainMenuOption.fromNumber(choice);
                if (option == null) {
                    // Defesa em profundidade — showMenu já valida o intervalo.
                    continue;
                }

                switch (option) {
                    case GERENCIAR_ALUNOS:     getStudentMenu().run();    break;
                    case GERENCIAR_PLANOS:     getPlanMenu().run();       break;
                    case GERENCIAR_MATRICULAS: getEnrollmentMenu().run(); break;
                    case RELATORIOS:           getReportsMenu().run();    break;
                    case SAIR:                 running = false;           break;
                }
            } catch (FitManagerException e) {
                ui.showError(e.getMessage());
            }
        }

        ui.showMessage("Obrigado por utilizar o FitManager! Até logo. 👋");
    }
}