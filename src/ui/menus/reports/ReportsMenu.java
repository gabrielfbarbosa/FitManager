package ui.menus.reports;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import domain.model.Student;
import domain.model.enums.PlanType;
import domain.model.filters.ActiveEnrollmentFilter;
import domain.model.filters.ByPlanTypeFilter;
import domain.model.filters.CancelledEnrollmentFilter;
import domain.model.filters.EnrollmentFilter;
import domain.model.filters.ExpiredEnrollmentFilter;
import domain.model.filters.PendingBalanceFilter;
import domain.model.plans.Plan;
import ui.screen.InputParser;
import ui.screen.UserInterface;

import java.util.ArrayList;

/**
 * Menu de relatórios do sistema.
 *
 * Consolida as principais formas de visualização dos dados cadastrados.
 * As consultas são claras e objetivas, utilizando getSummary() da interface
 * Summarizable para listagens compactas e toString() para consultas detalhadas.
 *
 * Utiliza filtros polimórficos (EnrollmentFilter) para selecionar
 * matrículas por diferentes critérios, eliminando duplicação de código.
 *
 * A lógica de filtragem e seleção reside nos serviços — o ReportsMenu
 * solicita os dados ao FitManager, que os obtém dos serviços e os repassa
 * para exibição.
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
            StringBuilder menuOptions = new StringBuilder();
            for (ReportsMenuOption opt : ReportsMenuOption.values()) {
                menuOptions.append(opt.getNumber()).append(" - ").append(opt.getOptionName()).append("\n");
            }
            String input = ui.showMenu("> RELATÓRIOS", menuOptions.toString());

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
                case LISTAR_ALUNOS:        listAllStudents();          break;
                case LISTAR_PLANOS:        listAllPlans();             break;
                case LISTAR_MATRICULAS:    listAllEnrollments();       break;
                case MATRICULAS_ATIVAS:    showFilteredEnrollments(new ActiveEnrollmentFilter());    break;
                case MATRICULAS_CANCELADAS:showFilteredEnrollments(new CancelledEnrollmentFilter()); break;
                case SALDO_PENDENTE:       showFilteredEnrollments(new PendingBalanceFilter());      break;
                case POR_TIPO_PLANO:       filterByPlanType();         break;
                case VENCIDAS:             showFilteredEnrollments(new ExpiredEnrollmentFilter());    break;
                case CONSULTAR_ALUNO:      findStudentByCpf();         break;
                case CONSULTAR_PLANO:      findPlanByName();           break;
                case CONSULTAR_MATRICULA:  findActiveEnrollment();     break;
                case ESTATISTICAS:         showStatistics();           break;
                case VOLTAR:               running = false;            break;
            }
        }
    }

    // ============================
    // Listagens (usam getSummary)
    // ============================

    /**
     * Lista todos os alunos ativos usando getSummary() para exibição compacta.
     */
    private void listAllStudents() {
        OperationResult result = fitManager.listAllStudents();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Student> students = (ArrayList<Student>) result.getData();
        StringBuilder message = new StringBuilder("> TODOS OS ALUNOS\n");
        message.append("Total: ").append(students.size()).append(" aluno(s)\n\n");

        for (int i = 0; i < students.size(); i++) {
            message.append(i + 1).append(". ").append(students.get(i).getSummary());
            if (i < students.size() - 1) {
                message.append("\n");
            }
        }

        ui.showScrollableMessage(message.toString());
    }

    /**
     * Lista todos os planos cadastrados usando getSummary() para exibição compacta.
     */
    private void listAllPlans() {
        OperationResult result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Plan> plans = (ArrayList<Plan>) result.getData();
        StringBuilder message = new StringBuilder("> TODOS OS PLANOS\n");
        message.append("Total: ").append(plans.size()).append(" plano(s)\n\n");

        for (int i = 0; i < plans.size(); i++) {
            message.append(i + 1).append(". ").append(plans.get(i).getSummary());
            if (i < plans.size() - 1) {
                message.append("\n");
            }
        }

        ui.showScrollableMessage(message.toString());
    }

    /**
     * Lista todas as matrículas (ativas e canceladas) usando getSummary().
     */
    private void listAllEnrollments() {
        OperationResult result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        StringBuilder message = new StringBuilder("> TODAS AS MATRÍCULAS\n");
        message.append("Total: ").append(enrollments.size()).append(" matrícula(s)\n\n");

        for (int i = 0; i < enrollments.size(); i++) {
            message.append(i + 1).append(". ").append(enrollments.get(i).getSummary());
            if (i < enrollments.size() - 1) {
                message.append("\n");
            }
        }

        ui.showScrollableMessage(message.toString());
    }

    // ============================
    // Consultas (usam toString)
    // ============================

    /**
     * Consulta um aluno pelo CPF e exibe seus dados completos (toString).
     */
    private void findStudentByCpf() {
        String cpf = ui.getInput("Digite o CPF do aluno para consulta:");
        if (cpf == null || cpf.trim().isEmpty()) {
            ui.showError("Insira um CPF");
            return;
        }

        OperationResult result = fitManager.findStudentByCpf(cpf);

        if (result.isSuccess()) {
            Student student = (Student) result.getData();
            ui.showMessage("Aluno encontrado:\n\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Consulta um plano pelo nome e exibe seus dados completos (toString).
     */
    private void findPlanByName() {
        String name = ui.getInput("Digite o nome do plano para consulta:");
        if (name == null || name.trim().isEmpty()) {
            ui.showError("Insira um plano");
            return;
        }

        OperationResult result = fitManager.findPlanByName(name);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage("Plano encontrado:\n\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF e exibe dados completos (toString).
     */
    private void findActiveEnrollment() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null || cpf.trim().isEmpty()) {
            ui.showError("Insira um CPF");
            return;
        }

        OperationResult result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage("Matrícula ativa encontrada:\n\n" + enrollment.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Filtros polimórficos
    // ============================

    /**
     * Método genérico que exibe matrículas filtradas por qualquer EnrollmentFilter.
     * Usa getSummary() para exibição compacta das matrículas filtradas.
     *
     * @param filter filtro polimórfico a ser aplicado
     */
    private void showFilteredEnrollments(EnrollmentFilter filter) {
        OperationResult result = fitManager.listEnrollmentsByFilter(filter);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        StringBuilder message = new StringBuilder();
        message.append("> ").append(filter.getDescription().toUpperCase()).append("\n");
        message.append("Total: ").append(enrollments.size()).append(" matrícula(s)\n\n");

        for (int i = 0; i < enrollments.size(); i++) {
            message.append(i + 1).append(". ").append(enrollments.get(i).getSummary());
            if (i < enrollments.size() - 1) {
                message.append("\n");
            }
        }

        ui.showScrollableMessage(message.toString());
    }

    /**
     * Fluxo de filtro por tipo de plano.
     * Exibe os tipos disponíveis e permite ao usuário escolher um,
     * depois aplica o ByPlanTypeFilter correspondente.
     */
    private void filterByPlanType() {
        StringBuilder options = new StringBuilder("Selecione o tipo de plano:\n\n");
        PlanType[] types = PlanType.values();
        for (int i = 0; i < types.length; i++) {
            options.append(i + 1).append(" - ").append(types[i].getLabel()).append("\n");
        }

        String input = ui.getInput(options.toString());
        if (input == null || input.trim().isEmpty()) {
            ui.showError("Selecione um plano");
            return;
        }

        if (!InputParser.isNumeric(input)) {
            ui.showError("Opção inválida. Digite um número de 1 a " + types.length + ".");
            return;
        }

        int choice = Integer.parseInt(input.trim());
        if (choice < 1 || choice > types.length) {
            ui.showError("Opção inválida. Escolha de 1 a " + types.length + ".");
            return;
        }

        PlanType selectedType = types[choice - 1];
        showFilteredEnrollments(new ByPlanTypeFilter(selectedType));
    }

    // ============================
    // Estatísticas
    // ============================

    /**
     * Exibe estatísticas gerais do sistema.
     */
    private void showStatistics() {
        OperationResult result = fitManager.getSystemStatistics();

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }
}
