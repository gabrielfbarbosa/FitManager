package ui.menus.reports;

import application.FitManager;
import application.OperationResult;
import application.reports.FinancialReport;
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
import exceptions.FitManagerException;
import ui.screen.UserInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final int INAUGURATION_MONTH = 3;
    private static final int INAUGURATION_YEAR = 2026;

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
            try {
                StringBuilder menuOptions = new StringBuilder();
                for (ReportsMenuOption opt : ReportsMenuOption.values()) {
                    menuOptions.append(opt.getNumber()).append(" - ").append(opt.getOptionName()).append("\n");
                }
                Integer choice = ui.showMenu("> RELATÓRIOS", menuOptions.toString(), ReportsMenuOption.values().length);

                if (choice == null) { running = false; continue; }

                ReportsMenuOption option = ReportsMenuOption.fromNumber(choice);
                if (option == null) continue;

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
                    case RELATORIO_FINANCEIRO: monthlyFinancialReport();   break;
                    case VOLTAR:               running = false;            break;
                }
            } catch (FitManagerException e) {
                ui.showError(e.getMessage());
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
        OperationResult<ArrayList<Student>> result = fitManager.listAllStudents();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Student> students = result.getData();
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
        OperationResult<ArrayList<Plan>> result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Plan> plans = result.getData();
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
        OperationResult<ArrayList<Enrollment>> result = fitManager.listAllEnrollments();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = result.getData();
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
        String cpf = askValidCpf("Digite o CPF do aluno para consulta:");
        if (cpf == null) return;

        OperationResult<Student> result = fitManager.findStudentByCpf(cpf);

        if (result.isSuccess()) {
            Student student = result.getData();
            ui.showMessage("Aluno encontrado:\n\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Consulta um plano pelo nome e exibe seus dados completos (toString).
     */
    private void findPlanByName() {
        String name = ui.getInput("Digite o nome do plano para consulta:", "Nome do plano");
        if (name == null) return;

        OperationResult<Plan> result = fitManager.findPlanByName(name);

        if (result.isSuccess()) {
            Plan plan = result.getData();
            ui.showMessage("Plano encontrado:\n\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF e exibe dados completos (toString).
     */
    private void findActiveEnrollment() {
        String cpf = askValidCpf("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult<Enrollment> result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = result.getData();
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
        OperationResult<ArrayList<Enrollment>> result = fitManager.listEnrollmentsByFilter(filter);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = result.getData();
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

        Integer choice = ui.showMenu("> FILTRO POR TIPO DE PLANO", options.toString(), types.length);
        if (choice == null) return;

        PlanType selectedType = types[choice - 1];
        showFilteredEnrollments(new ByPlanTypeFilter(selectedType));
    }

    /**
     * Verificar se um cpf é valido, e solicitar novamente
     * caso não for valido
     */
    private String askValidCpf(String prompt) {

        while (true) {

            String cpf = ui.getInput(prompt, "CPF");

            OperationResult<String> result = fitManager.validateCpf(cpf);

            if (result.isSuccess()) {
                return result.getData();
            }

            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Estatísticas
    // ============================

    /**
     * Exibe estatísticas gerais do sistema.
     */
    private void showStatistics() {
        OperationResult<Void> result = fitManager.getSystemStatistics();

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    // ============================
    // Relatório Financeiro Mensal
    // ============================

    /**
     * Fluxo do relatório financeiro mensal.
     * Solicita mês/ano (com validação no próprio getInt), gera o relatório
     * via {@link FitManager#generateMonthlyReport(int, int)} e exibe o
     * resultado formatado. Período sem dados é exibido normalmente, com
     * todas as métricas zeradas e uma nota informativa — não é tratado
     * como erro.
     *
     * Após a exibição, oferece ao usuário a opção de exportar o relatório
     * para um arquivo CSV em {@code data/reports/}.
     */
    private void monthlyFinancialReport() {
        Integer month = ui.getInt("Digite o mês (1-12):", "Mês");
        if (month == null) return;
        if (month < 1 || month > 12) {
            ui.showError("O mês deve estar entre 1 e 12.");
            return;
        }

        Integer year = ui.getInt("Digite o ano (ex: 2026):", "Ano");
        if (year == null) return;

        // A FitManager foi inaugurada em 01/03/2026: só há relatórios a partir
        // desse período. Anos seguintes (2027+) têm todos os meses disponíveis,
        // inclusive janeiro e fevereiro.
        if (year < INAUGURATION_YEAR
                || (year == INAUGURATION_YEAR && month < INAUGURATION_MONTH)) {
            ui.showError("A FitManager foi inaugurada em "
                    + String.format("%02d/%d", INAUGURATION_MONTH, INAUGURATION_YEAR)
                    + ". Só é possível gerar relatórios a partir dessa data.");
            return;
        }

        OperationResult<FinancialReport> result = fitManager.generateMonthlyReport(month, year);
        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        FinancialReport report = result.getData();
        ui.showScrollableMessage(report.format());

        String exportar = ui.getInput(
                "Deseja exportar o relatório para um arquivo CSV?\n" +
                        "Digite 'S' para confirmar ou qualquer outra tecla para pular:");
        if (exportar != null && exportar.trim().equalsIgnoreCase("S")) {
            exportReport(report);
        }
    }

    /**
     * Exporta o relatório para {@code data/reports/relatorio_financeiro_<mes>_<ano>.csv}.
     * Falhas de escrita são exibidas ao usuário sem encerrar o programa.
     */
    private void exportReport(FinancialReport report) {
        String fileName = String.format("relatorio_financeiro_%02d_%d.csv",
                report.getMonth(), report.getYear());
        Path dir = Paths.get("data", "reports");
        Path path = dir.resolve(fileName);
        try {
            Files.createDirectories(dir);
            Files.writeString(path, report.toCsv());
            ui.showMessage("✅ Relatório exportado para: " + path.toAbsolutePath());
        } catch (IOException e) {
            ui.showError("Falha ao exportar o relatório: " + e.getMessage());
        }
    }
}
