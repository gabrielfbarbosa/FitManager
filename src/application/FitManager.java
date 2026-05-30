package application;

import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PlanType;
import domain.model.enums.PaymentType;
import domain.model.filters.EnrollmentFilter;
import domain.model.payments.Payment;
import domain.model.plans.Plan;
import domain.model.Student;
import domain.model.Enrollment;
import exceptions.DuplicatedEnrollmentException;
import exceptions.InvalidFormatFieldException;
import exceptions.RequiredFieldException;
import exceptions.StudentWithActiveEnrollmentException;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * Ponto de entrada único para todas as operações do sistema.
 *
 * Os menus nunca acessam os serviços ou as classes de domínio diretamente —
 * tudo passa pelo FitManager. Isso centraliza a coordenação e evita que
 * lógica de validação fique espalhada pelo código.
 *
 * Política de exceções na camada de coordenação:
 * - Quando uma operação envolve coordenação entre serviços (verificar
 *   matrícula ativa antes de remover aluno, antes de matricular novamente),
 *   o FitManager é quem lança a {@code BusinessException} apropriada.
 * - Datas em string fornecidas pelos menus são convertidas aqui; falhas
 *   de parsing são relançadas como {@link InvalidFormatFieldException}.
 * - Campos obrigatórios da fachada (CPF, data) são validados na entrada e
 *   lançam {@link RequiredFieldException}.
 */
public class FitManager {

    private StudentService studentService;
    private PlanService planService;
    private EnrollmentService enrollmentService;

    public FitManager() {
        this.studentService = new StudentService();
        this.planService = new PlanService();
        this.enrollmentService = new EnrollmentService();
    }

    // ============================
    // Operações de Alunos
    // ============================

    /**
     * Registra um novo aluno.
     * Delega a validação e criação ao StudentService.
     */
    public OperationResult<Student> registerStudent(
            String name,
            String cpf,
            String contact,
            String birthDate
    ) {
        return studentService.registerStudent(name, cpf, contact, birthDate);
    }

    /**
     * Consulta um aluno pelo CPF.
     */
    public OperationResult<Student> findStudentByCpf(String cpf) {
        return studentService.findByCpf(cpf);
    }

    /**
     * Atualiza os dados de um aluno (nome e/ou contato).
     */
    public OperationResult<Student> updateStudent(String cpf, String newName, String newContact) {
        return studentService.updateStudent(cpf, newName, newContact);
    }

    /**
     * Remove (desativa) um aluno.
     * Coordena com o EnrollmentService para impedir remoção de aluno com
     * matrícula ativa — situação que é lançada como {@link StudentWithActiveEnrollmentException}.
     */
    public OperationResult<Void> removeStudent(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        String cleanCpf = Student.cleanCpf(cpf);

        // Verifica se o aluno existe e está ativo
        OperationResult<Student> findResult = studentService.findByCpf(cleanCpf);
        if (!findResult.isSuccess()) {
            return new OperationResult<>(false, findResult.getMessage());
        }

        // Verifica se o aluno possui matrícula ativa
        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            throw new StudentWithActiveEnrollmentException(cleanCpf);
        }

        return studentService.removeStudent(cleanCpf);
    }

    /**
     * Lista todos os alunos ativos.
     */
    public OperationResult<ArrayList<Student>> listAllStudents() {
        return studentService.listAll();
    }

    // ============================
    // Operações de Planos
    // ============================

    /**
     * Registra um novo plano.
     */
    public OperationResult<Plan> registerPlan(
            String name,
            String description,
            PlanType type,
            int minimumDuration,
            double pricePerMonth
    ) {
        return planService.registerPlan(name, description, type, minimumDuration, pricePerMonth);
    }

    /**
     * Consulta um plano pelo nome.
     */
    public OperationResult<Plan> findPlanByName(String name) {
        return planService.findByName(name);
    }

    /**
     * Atualiza o preço mensal de um plano.
     * Não afeta matrículas já registradas — totalPrice é fixado na criação do Enrollment.
     */
    public OperationResult<Plan> updatePlanPrice(String name, double newPrice) {
        return planService.updatePrice(name, newPrice);
    }

    /**
     * Lista todos os planos cadastrados.
     */
    public OperationResult<ArrayList<Plan>> listAllPlans() {
        return planService.listAll();
    }

    // ============================
    // Operações de Matrículas
    // ============================

    /**
     * Realiza a matrícula de um aluno em um plano.
     * Captura {@link DateTimeParseException} ao converter {@code startDateStr}
     * e relança como {@link InvalidFormatFieldException}, mantendo a falha
     * dentro da hierarquia do FitManager para o catch único do menu.
     */
    public OperationResult<Enrollment> enrollStudent(
            String cpf,
            String planName,
            String startDateStr,
            int durationMonths,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {

        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        if (planName == null || planName.isBlank()) {
            throw new RequiredFieldException("Nome do plano");
        }
        if (startDateStr == null || startDateStr.isBlank()) {
            throw new RequiredFieldException("Data de início");
        }

        String cleanCpf = Student.cleanCpf(cpf);

        OperationResult<Student> studentResult = studentService.findByCpf(cleanCpf);
        if (!studentResult.isSuccess()) {
            return new OperationResult<>(false, studentResult.getMessage());
        }

        OperationResult<Plan> planResult = planService.findByName(planName);
        if (!planResult.isSuccess()) {
            return new OperationResult<>(false, planResult.getMessage());
        }

        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            throw new DuplicatedEnrollmentException(cleanCpf);
        }

        LocalDate startDate;
        try {
            startDate = DateFormatter.parseDate(startDateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatFieldException("Data de início", DateFormatter.DATE_PATTERN + " (ex.: 13/08/2026)");
        }

        Student student = studentResult.getData();
        Plan plan = planResult.getData();
        return enrollmentService.enroll(student, plan, startDate, durationMonths,
                initialAmount, paymentType, paymentDescription, paymentData);
    }

    /**
     * Cancela uma matrícula ativa.
     */
    public OperationResult<Enrollment> cancelEnrollment(int enrollmentCode) {
        return enrollmentService.cancelEnrollment(enrollmentCode);
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF.
     */
    public OperationResult<Enrollment> findActiveEnrollmentByStudent(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.findActiveByStudentCpf(cleanCpf);
    }

    /**
     * Lista o histórico de matrículas de um aluno.
     */
    public OperationResult<ArrayList<Enrollment>> listEnrollmentHistory(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.listHistoryByStudent(cleanCpf);
    }

    /**
     * Registra um novo pagamento para uma matrícula.
     *
     * @param paymentData dados adicionais do pagamento (variam por tipo)
     */
    public OperationResult<Payment> registerPayment(
            int enrollmentCode,
            double amount,
            PaymentType paymentType,
            String description,
            String[] paymentData
    ) {
        return enrollmentService.registerPayment(enrollmentCode, amount, paymentType, description, paymentData);
    }

    // ============================
    // Operações de Relatórios
    // ============================

    /**
     * Lista matrículas usando um filtro polimórfico.
     * Delega ao EnrollmentService.listByFilter(), que aplica o critério
     * do filtro a toda a coleção de matrículas.
     *
     * Permite adicionar novos relatórios sem alterar o FitManager —
     * basta criar uma nova implementação de EnrollmentFilter.
     *
     * @param filter filtro polimórfico a ser aplicado
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult<ArrayList<Enrollment>> listEnrollmentsByFilter(EnrollmentFilter filter) {
        return enrollmentService.listByFilter(filter);
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    public OperationResult<ArrayList<Enrollment>> listAllEnrollments() {
        return enrollmentService.listAll();
    }

    /**
     * Calcula e retorna estatísticas gerais do sistema.
     * O texto formatado é colocado na mensagem do OperationResult; não há
     * dado adicional, daí o uso de {@code OperationResult<Void>}.
     */
    public OperationResult<Void> getSystemStatistics() {
        OperationResult<ArrayList<Student>> allStudents = listAllStudents();
        OperationResult<ArrayList<Enrollment>> allEnrollments = listAllEnrollments();
        OperationResult<ArrayList<Plan>> allPlans = listAllPlans();

        int totalStudents = 0;
        if (allStudents.isSuccess()) {
            totalStudents = allStudents.getData().size();
        }

        int totalEnrollments = 0;
        int totalActiveEnrollments = 0;
        double totalBalance = 0;
        if (allEnrollments.isSuccess()) {
            ArrayList<Enrollment> enrollments = allEnrollments.getData();
            totalEnrollments = enrollments.size();
            for (Enrollment enrollment : enrollments) {
                if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                    totalActiveEnrollments++;
                }
                totalBalance += enrollment.calculateBalance();
            }
        }

        int totalPlans = 0;
        if (allPlans.isSuccess()) {
            totalPlans = allPlans.getData().size();
        }

        String stats = "ESTATÍSTICAS DO SISTEMA\n\n" +
                "Alunos Cadastrados: " + totalStudents + "\n" +
                "Planos Disponíveis: " + totalPlans + "\n" +
                "Total de Matrículas: " + totalEnrollments + "\n" +
                "Matrículas Ativas: " + totalActiveEnrollments + "\n" +
                "Saldo Pendente Total: " + CurrencyFormatter.formatCurrency(totalBalance);

        return new OperationResult<>(true, stats);
    }
}