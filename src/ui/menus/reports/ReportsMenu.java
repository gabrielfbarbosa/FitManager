package ui.menus.reports;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de relatórios do sistema.
 * Apresenta estatísticas e listas de matrículas com diferentes filtros.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para consultar dados).
 */
public class ReportsMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public ReportsMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de relatórios.
     * Exibe opções até o usuário escolher "Voltar".
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
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + ReportsMenuOption.values().length + ".");
                continue;
            }

            ReportsMenuOption option = ReportsMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + ReportsMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case ALUNOS_ATIVOS:     listActiveEnrollments();    break;
                case SALDO_PENDENTE:    listPendingBalance();       break;
                case TODAS_MATRICULAS:  listAllEnrollments();       break;
                case ESTATISTICAS:      showStatistics();           break;
                case VOLTAR:            running = false;            break;
            }
        }
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    private void listAllEnrollments() {
        OperationResult result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> TODAS AS MATRÍCULAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Lista apenas as matrículas ativas.
     */
    private void listActiveEnrollments() {
        OperationResult result = fitManager.listActiveEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> MATRÍCULAS ATIVAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s) ativa(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Lista as matrículas com saldo pendente.
     */
    private void listPendingBalance() {
        OperationResult result = fitManager.listEnrollmentsWithPendingBalance();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> MATRÍCULAS COM SALDO PENDENTE\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += buildEnrollmentDetails(enrollments.get(i));
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Exibe estatísticas gerais do sistema.
     */
    private void showStatistics() {
        OperationResult result = fitManager.getSystemStatistics();

        if (result.isSuccess()) {
            String stats = result.getMessage();
            ui.showMessage(stats);
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Constrói uma representação detalhada de uma matrícula para exibição em relatórios.
     */
    private String buildEnrollmentDetails(Enrollment enrollment) {
        String formatDate = "%02d/%02d/%04d";
        return "Código: " + enrollment.getCode() + "\n" +
                "CPF do Aluno: " + enrollment.getStudentCpf() + "\n" +
                "Plano: " + enrollment.getPlanName() + "\n" +
                "Data Início: " + String.format(formatDate,
                enrollment.getStartDate().getDayOfMonth(),
                enrollment.getStartDate().getMonthValue(),
                enrollment.getStartDate().getYear()) + "\n" +
                "Data Fim: " + String.format(formatDate,
                enrollment.getEndDate().getDayOfMonth(),
                enrollment.getEndDate().getMonthValue(),
                enrollment.getEndDate().getYear()) + "\n" +
                "Duração: " + enrollment.getDurationMonths() +
                (enrollment.getDurationMonths() == 1 ? " mês" : " meses") + "\n" +
                "Preço Total: R$ " + String.format("%.2f", enrollment.getTotalPrice()) + "\n" +
                "Saldo Pendente: R$ " + String.format("%.2f", enrollment.calculateBalance()) + "\n" +
                "Status: " + enrollment.getStatus().getLabel() + "\n" +
                "Pagamentos: " + enrollment.getPayments().size();
    }
}