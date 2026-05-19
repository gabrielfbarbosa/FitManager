package application;

import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PlanType;
import domain.model.enums.PaymentType;
import domain.model.filters.EnrollmentFilter;
import domain.model.plans.Plan;
import domain.model.Student;
import domain.model.Enrollment;
import util.CurrencyFormatter;

import java.time.LocalDate;
import java.util.ArrayList;

import util.DateFormatter;

/**
 * Ponto de entrada único para todas as operações do sistema.
 *
 * Os menus nunca acessam os serviços ou as classes de domínio diretamente —
 * tudo passa pelo FitManager. Isso centraliza a coordenação e evita que
 * lógica de validação fique espalhada pelo código.
 *
 * Relação de composição com os serviços: eles são criados e gerenciados
 * pelo próprio FitManager e não existem de forma independente.
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
    public OperationResult registerStudent(String name, String cpf,
                                           String contact, String birthDate) {
        return studentService.registerStudent(name, cpf, contact, birthDate);
    }

    /**
     * Consulta um aluno pelo CPF.
     */
    public OperationResult findStudentByCpf(String cpf) {
        return studentService.findByCpf(cpf);
    }

    /**
     * Atualiza os dados de um aluno (nome e/ou contato).
     */
    public OperationResult updateStudent(String cpf, String newName, String newContact) {
        return studentService.updateStudent(cpf, newName, newContact);
    }

    /**
     * Remove (desativa) um aluno.
     *
     * Coordenação entre serviços: o FitManager consulta o EnrollmentService
     * para verificar matrículas ativas antes de delegar ao StudentService.
     * Os serviços não se comunicam diretamente entre si.
     */
    public OperationResult removeStudent(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);

        // Verifica se o aluno existe e está ativo
        OperationResult findResult = studentService.findByCpf(cleanCpf);
        if (!findResult.isSuccess()) {
            return findResult;
        }

        // Verifica se o aluno possui matrícula ativa
        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            return new OperationResult(false,
                    "Não é possível remover o aluno: ele possui matrícula ativa.\n"
                            + "Cancele a matrícula antes de remover o aluno.");
        }

        return studentService.removeStudent(cleanCpf);
    }

    /**
     * Lista todos os alunos ativos.
     */
    public OperationResult listAllStudents() {
        return studentService.listAll();
    }

    // ============================
    // Operações de Planos
    // ============================

    /**
     * Registra um novo plano.
     */
    public OperationResult registerPlan(String name, String description, PlanType type,
                                        int minimumDuration, double pricePerMonth) {
        return planService.registerPlan(name, description, type, minimumDuration, pricePerMonth);
    }

    /**
     * Consulta um plano pelo nome.
     */
    public OperationResult findPlanByName(String name) {
        return planService.findByName(name);
    }

    /**
     * Atualiza o preço mensal de um plano.
     * Não afeta matrículas já registradas — totalPrice é fixado na criação do Enrollment.
     */
    public OperationResult updatePlanPrice(String name, double newPrice) {
        return planService.updatePrice(name, newPrice);
    }

    /**
     * Lista todos os planos cadastrados.
     */
    public OperationResult listAllPlans() {
        return planService.listAll();
    }

    // ============================
    // Operações de Matrículas
    // ============================

    /**
     * Realiza a matrícula de um aluno em um plano.
     *
     * @param paymentData dados adicionais do pagamento (variam por tipo)
     */
    public OperationResult enrollStudent(String cpf, String planName, String startDateStr,
                                         int durationMonths, double initialAmount,
                                         PaymentType paymentType, String paymentDescription,
                                         String[] paymentData) {

        String cleanCpf = Student.cleanCpf(cpf);

        OperationResult studentResult = studentService.findByCpf(cleanCpf);
        if (!studentResult.isSuccess()) {
            return studentResult;
        }

        OperationResult planResult = planService.findByName(planName);
        if (!planResult.isSuccess()) {
            return planResult;
        }

        if (enrollmentService.hasActiveEnrollment(cleanCpf)) {
            return new OperationResult(false,
                    "O aluno já possui uma matrícula ativa. "
                            + "Cancele a matrícula atual antes de realizar uma nova.");
        }

        LocalDate startDate = DateFormatter.parseDate(startDateStr);

        Student student = (Student) studentResult.getData();
        Plan plan = (Plan) planResult.getData();
        return enrollmentService.enroll(student, plan, startDate, durationMonths,
                initialAmount, paymentType, paymentDescription, paymentData);
    }

    /**
     * Cancela uma matrícula ativa.
     */
    public OperationResult cancelEnrollment(int enrollmentCode) {
        return enrollmentService.cancelEnrollment(enrollmentCode);
    }

    /**
     * Consulta a matrícula ativa de um aluno pelo CPF.
     */
    public OperationResult findActiveEnrollmentByStudent(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.findActiveByStudentCpf(cleanCpf);
    }

    /**
     * Lista o histórico de matrículas de um aluno.
     */
    public OperationResult listEnrollmentHistory(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);
        return enrollmentService.listHistoryByStudent(cleanCpf);
    }

    /**
     * Registra um novo pagamento para uma matrícula.
     *
     * @param paymentData dados adicionais do pagamento (variam por tipo)
     */
    public OperationResult registerPayment(
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
    public OperationResult listEnrollmentsByFilter(EnrollmentFilter filter) {
        return enrollmentService.listByFilter(filter);
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    public OperationResult listAllEnrollments() {
        return enrollmentService.listAll();
    }

    /**
     * Lista apenas as matrículas ativas.
     */
    public OperationResult listActiveEnrollments() {
        return enrollmentService.listActive();
    }

    /**
     * Lista as matrículas com saldo pendente.
     */
    public OperationResult listEnrollmentsWithPendingBalance() {
        return enrollmentService.listWithPendingBalance();
    }

    /**
     * Calcula e retorna estatísticas gerais do sistema.
     */
    public OperationResult getSystemStatistics() {
        OperationResult allStudents = listAllStudents();
        OperationResult allEnrollments = listAllEnrollments();
        OperationResult allPlans = listAllPlans();

        int totalStudents = 0;
        if (allStudents.isSuccess()) {
            ArrayList<Student> students = (ArrayList<Student>) allStudents.getData();
            totalStudents = students.size();
        }

        int totalEnrollments = 0;
        int totalActiveEnrollments = 0;
        double totalBalance = 0;
        if (allEnrollments.isSuccess()) {
            ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) allEnrollments.getData();
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
            ArrayList<Plan> plans = (ArrayList<Plan>) allPlans.getData();
            totalPlans = plans.size();
        }

        String stats = "ESTATÍSTICAS DO SISTEMA\n\n" +
                "Alunos Cadastrados: " + totalStudents + "\n" +
                "Planos Disponíveis: " + totalPlans + "\n" +
                "Total de Matrículas: " + totalEnrollments + "\n" +
                "Matrículas Ativas: " + totalActiveEnrollments + "\n" +
                "Saldo Pendente Total: " + CurrencyFormatter.formatCurrency(totalBalance);

        return new OperationResult(true, stats);
    }
}