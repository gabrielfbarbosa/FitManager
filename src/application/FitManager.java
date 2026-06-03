package application;

import application.reports.FinancialReport;
import application.services.StudentService;
import application.services.PlanService;
import application.services.EnrollmentService;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PlanType;
import domain.model.enums.PaymentType;
import domain.model.filters.EnrollmentFilter;
import domain.model.payments.Payment;
import domain.model.payments.PaymentFactory;
import domain.model.plans.Plan;
import domain.model.Student;
import domain.model.Enrollment;
import exceptions.DuplicatedEnrollmentException;
import exceptions.InvalidFormatFieldException;
import exceptions.PersistenceException;
import exceptions.RequiredFieldException;
import exceptions.StudentWithActiveEnrollmentException;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        // conecta o repositório de alunos para validar CPFs
        this.enrollmentService.linkStudentRepository(this.studentService.getRepository());
        // Conecta o repositório de planos ao de matrículas para que a leitura
        // do arquivo de matrículas consiga resolver as referências de Plan.
        this.enrollmentService.linkPlanRepository(this.planService.getRepository());
    }

    // ============================
    // Coordenação de persistência
    // ============================

    /**
     * Carrega todos os repositórios na ordem correta:
     * primeiro alunos e planos (independentes), depois matrículas
     * (que referenciam planos). Arquivos ausentes são tratados como
     * primeira execução (repositórios iniciam vazios sem erro).
     *
     * @throws PersistenceException se algum arquivo estiver corrompido
     *         ou se ocorrer falha de leitura
     */
    public void loadAll() throws PersistenceException {
        studentService.getRepository().load();
        planService.getRepository().load();
        enrollmentService.getRepository().load();
    }

    /**
     * Persiste todos os repositórios na ordem reversa de dependência:
     * matrículas primeiro (pra garantir que estejam fechadas), depois
     * planos e alunos. Os recursos são fechados via try-with-resources
     * em cada repositório, mesmo em caso de falha.
     *
     * @throws PersistenceException se ocorrer falha de escrita em
     *         qualquer um dos repositórios
     */
    public void saveAll() throws PersistenceException {
        enrollmentService.getRepository().save();
        planService.getRepository().save();
        studentService.getRepository().save();
    }

    /**
     * Indica se TODOS os repositórios estão vazios — usado pelo bootstrap
     * para decidir se deve popular o sistema com dados de demonstração
     * (DataMock) ou se já há dados persistidos do uso anterior.
     */
    public boolean isEmpty() {
        return studentService.getRepository().isEmpty()
                && planService.getRepository().isEmpty()
                && enrollmentService.getRepository().isEmpty();
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

    /**
     * Valida um CPF e retorna o CPF limpo.
     */
    public OperationResult<String> validateCpf(String cpf) {

        if (cpf.isBlank()) {
            return new OperationResult<>(
                    false,
                    "O campo CPF é obrigatório."
            );
        }

        String cleanCpf = Student.cleanCpf(cpf);

        if (!Student.validateCpf(cleanCpf)) {
            return new OperationResult<>(
                    false,
                    "CPF inválido. Informe um CPF válido."
            );
        }

        return new OperationResult<>(
                true,
                "CPF válido.",
                cleanCpf
        );
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

    /**
     * Gera o relatório financeiro mensal consolidando todas as métricas
     * exigidas pelo enunciado (receita total, receita por tipo de plano,
     * receita por forma de pagamento, taxas de processamento, matrículas
     * iniciadas e canceladas no período, tipos de plano mais contratados).
     *
     * Agregações são feitas via polimorfismo — {@code plan.getTypeName()}
     * e {@code payment.getTypeName()} — sem {@code instanceof} nem
     * {@code getClass()}.
     *
     * Período sem dados é um resultado válido: retorna {@code success = true}
     * com um {@link FinancialReport} zerado; jamais lança exceção ou
     * retorna {@code success = false} apenas por ausência de pagamentos.
     *
     * @param month mês do período (1-12)
     * @param year  ano do período (positivo)
     * @return OperationResult com o FinancialReport calculado, ou erro se
     *         os parâmetros estiverem fora do intervalo
     */
    public OperationResult<FinancialReport> generateMonthlyReport(int month, int year) {
        if (month < 1 || month > 12) {
            return new OperationResult<>(false, "Mês deve estar entre 1 e 12.");
        }
        if (year <= 0) {
            return new OperationResult<>(false, "Ano deve ser um valor positivo.");
        }

        FinancialReport report = new FinancialReport(month, year);

        OperationResult<ArrayList<Enrollment>> allResult = listAllEnrollments();
        if (allResult.isSuccess()) {
            for (Enrollment enrollment : allResult.getData()) {
                // Matrículas iniciadas no período (agrupa por tipo de plano via getTypeName).
                LocalDate start = enrollment.getStartDate();
                if (start != null && start.getMonthValue() == month && start.getYear() == year) {
                    report.addEnrollmentStarted(enrollment.getPlan().getTypeName());
                }

                // Matrículas canceladas no período.
                LocalDate cancelled = enrollment.getCancelledAt();
                if (cancelled != null && cancelled.getMonthValue() == month && cancelled.getYear() == year) {
                    report.incrementCancelled();
                }

                // Pagamentos no período — somam à receita, taxas e agrupamentos.
                String planTypeName = enrollment.getPlan().getTypeName();
                for (Payment payment : enrollment.getPayments()) {
                    LocalDateTime when = payment.getPaymentDate();
                    if (when != null && when.getMonthValue() == month && when.getYear() == year) {
                        report.addPayment(planTypeName, payment);
                    }
                }
            }
        }

        return new OperationResult<>(true, "Relatório financeiro gerado.", report);
    }

    /**
     * Insere uma matrícula totalmente montada (com código, status, cancelledAt
     * e pagamentos já populados) diretamente no repositório, sem aplicar as
     * validações do fluxo de negócio (data de início no passado, plano
     * existente, aluno cadastrado, matrícula ativa duplicada).
     *
     * <p>Uso EXCLUSIVO para popular dados de demonstração via DataMocks. É a
     * única "porta" mock-only exposta pelo FitManager — todas as demais
     * variações específicas de mockEnrollment (datas no passado, cancelamentos em
     * datas históricas, pagamentos retroativos) são montadas pelo próprio
     * mock usando o construtor de restauração de {@link Enrollment} e
     * {@link PaymentFactory}.</p>
     */
    public void mockEnrollment(Enrollment enrollment) {
        enrollmentService.mockEnrollment(enrollment);
    }
}