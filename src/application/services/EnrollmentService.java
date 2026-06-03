package application.services;

import application.OperationResult;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PaymentType;
import domain.model.filters.EnrollmentFilter;
import domain.model.Enrollment;
import domain.model.payments.Payment;
import domain.model.payments.PaymentFactory;
import domain.model.plans.Plan;
import domain.model.Student;
import exceptions.InvalidFormatFieldException;
import exceptions.RequiredFieldException;
import persistence.EnrollmentRepository;
import persistence.PlanRepository;
import persistence.StudentRepository;
import util.CurrencyFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Serviço responsável pelas operações específicas da entidade Enrollment.
 *
 * Delega o armazenamento e a persistência da coleção ao
 * {@link EnrollmentRepository}, composto como atributo interno (relação de
 * composição). O serviço cuida das regras de negócio (validação de parâmetros
 * da matrícula, regras de pagamento, taxa de cancelamento via polimorfismo);
 * o repositório cuida da coleção e — a partir da Etapa 4 — da persistência em
 * arquivo, incluindo o estado do contador estático {@code Enrollment.nextCode}.
 *
 * Política de comunicação de falhas:
 * - Argumentos obrigatórios nulos (aluno, plano, data) → {@link RequiredFieldException}.
 * - Strings de pagamento mal formadas internamente → capturadas como
 *   {@link NumberFormatException} e relançadas como {@link InvalidFormatFieldException}.
 * - Demais validações de domínio (valores não positivos, data anterior a hoje,
 *   duração abaixo do mínimo, matrícula cancelada não aceita pagamento) seguem
 *   usando {@link OperationResult} com {@code success = false}.
 * - A duplicidade de matrícula ativa é detectada pelo {@code FitManager}.
 */
public class EnrollmentService {

    private EnrollmentRepository repository;

    public EnrollmentService() {
        this.repository = new EnrollmentRepository();
    }

    /**
     * Expõe o repositório composto.
     * Utilizado pelo orquestrador (FitManager) para coordenar persistência.
     */
    public EnrollmentRepository getRepository() {
        return repository;
    }

    /**
     * Conecta o repositório de alunos ao repositório de matrículas, para que
     * a leitura do arquivo de matrículas consiga validar os CPFs dos alunos.
     * Chamado pelo {@code FitManager} na construção.
     */
    public void linkStudentRepository(StudentRepository studentRepository) {
        this.repository.setStudentRepository(studentRepository);
    }

    /**
     * Conecta o repositório de planos ao repositório de matrículas, para que
     * a leitura do arquivo de matrículas consiga resolver as referências de
     * {@link Plan} pelo nome. Chamado pelo {@code FitManager} na construção.
     */
    public void linkPlanRepository(PlanRepository planRepository) {
        this.repository.setPlanRepository(planRepository);
    }

    /**
     * Realiza a matrícula de um aluno em um plano.
     */
    public OperationResult<Enrollment> enroll(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {
        OperationResult<Void> validationResult = validateEnrollmentParams(
                student, plan, startDate, durationMonths);
        if (!validationResult.isSuccess()) {
            return new OperationResult<>(false, validationResult.getMessage());
        }

        OperationResult<Void> paymentValidation = validatePaymentParams(initialAmount, paymentType, paymentData);
        if (!paymentValidation.isSuccess()) {
            return new OperationResult<>(false, paymentValidation.getMessage());
        }

        double totalPrice = plan.calculateTotalPrice(durationMonths);

        Enrollment enrollment = new Enrollment(
                student.getCpf(),
                plan,
                startDate,
                durationMonths,
                totalPrice
        );

        // Cria pagamento inicial
        Payment initialPayment = createPaymentByType(
                initialAmount,
                LocalDateTime.now(),
                paymentType,
                paymentDescription,
                paymentData
        );
        enrollment.addPayment(initialPayment);

        repository.add(enrollment);

        String message = "✅ Matrícula realizada com sucesso!\n\n" +
                "Código: " + enrollment.getCode() + "\n" +
                "Aluno: " + student.getName() + "\n" +
                "Plano: " + plan.getName() + " (" + plan.getTypeName() + ")\n" +
                "Preço Total: " + CurrencyFormatter.formatCurrency(totalPrice) + "\n" +
                "Pagamento Inicial: " + CurrencyFormatter.formatCurrency(initialAmount) + "\n" +
                "Saldo Pendente: " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance()) + "\n\n" +
                initialPayment.getPaymentSummary();

        return new OperationResult<>(true, message, enrollment);
    }

    /**
     * Valida os parâmetros necessários para uma matrícula.
     * Argumentos nulos são tratados como campos obrigatórios ausentes.
     */
    private OperationResult<Void> validateEnrollmentParams(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths
    ) {
        if (student == null) {
            throw new RequiredFieldException("Aluno");
        }
        if (plan == null) {
            throw new RequiredFieldException("Plano");
        }
        if (startDate == null) {
            throw new RequiredFieldException("Data de início");
        }
        if (startDate.isBefore(LocalDate.now())) {
            return new OperationResult<>(false, "A data de início não pode ser anterior a hoje.");
        }
        if (durationMonths < plan.getMinimumDuration()) {
            return new OperationResult<>(false,
                    "A duração deve ser no mínimo " + plan.getMinimumDuration() +
                            (plan.getMinimumDuration() == 1 ? " mês" : " meses") + ".");
        }
        return new OperationResult<>(true, "ok");
    }

    /**
     * Instancia a subclasse correta de Payment com base no PaymentType.
     * Delega a decisão ao {@link PaymentFactory} — reaproveitado também pelo
     * {@code EnrollmentRepository} ao reconstruir pagamentos da persistência.
     *
     * As conversões {@code Integer.parseInt}/{@code Double.parseDouble} são
     * envoltas em {@code try-catch} para que qualquer {@link NumberFormatException}
     * vinda de dados mal formados seja relançada como {@link InvalidFormatFieldException}.
     */
    private Payment createPaymentByType(
            double amount,
            LocalDateTime paymentDate,
            PaymentType paymentType,
            String description,
            String[] paymentData
    ) {
        try {
            return PaymentFactory.create(paymentType, amount, paymentDate, description, paymentData);
        } catch (NumberFormatException e) {
            if (paymentType == PaymentType.CREDIT_CARD) {
                throw new InvalidFormatFieldException("Número de parcelas", "Número inteiro");
            }
            throw new InvalidFormatFieldException("Valor recebido", "Número decimal (ex.: 99,90)");
        }
    }

    /**
     * Cancela uma matrícula ativa.
     */
    public OperationResult<Enrollment> cancelEnrollment(int enrollmentCode) {
        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult<>(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult<>(false, "Esta matrícula já foi cancelada.");
        }

        double cancellationFee = enrollment.getPlan().getCancellationFee(enrollment);

        double totalPrice = enrollment.getTotalPrice();
        double totalPaid = totalPrice - enrollment.calculateBalance();
        double pendingBalance = enrollment.calculateBalance();

        enrollment.cancel();

        String message = "✅ Matrícula " + enrollmentCode + " cancelada com sucesso!\n\n" +
                "RESUMO FINANCEIRO DO CANCELAMENTO\n" +
                "Valor total contratado: " + CurrencyFormatter.formatCurrency(totalPrice) + "\n" +
                "Total já pago: " + CurrencyFormatter.formatCurrency(totalPaid) + "\n";

        if (pendingBalance > 0) {
            message += "Saldo pendente: " + CurrencyFormatter.formatCurrency(pendingBalance) + "\n";
        } else if (pendingBalance < 0) {
            message += "Crédito do aluno: " + CurrencyFormatter.formatCurrency(Math.abs(pendingBalance)) + "\n";
        } else {
            message += "Saldo: quitado\n";
        }

        if (cancellationFee > 0) {
            message += "\nTaxa de cancelamento: " + CurrencyFormatter.formatCurrency(cancellationFee) +
                    "\nMotivo: cancelamento antes da metade do período contratado (" +
                    enrollment.getPlan().getTypeName() + ")";
        }

        return new OperationResult<>(true, message, enrollment);
    }

    /**
     * Busca a matrícula ativa de um aluno pelo CPF.
     */
    public OperationResult<Enrollment> findActiveByStudentCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }

        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.getStudentCpf().equals(cpf) &&
                    enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return new OperationResult<>(true, "Matrícula encontrada.", enrollment);
            }
        }

        return new OperationResult<>(false, "Nenhuma matrícula ativa encontrada para este aluno.");
    }

    /**
     * Verifica se um aluno possui matrícula ativa.
     */
    public boolean hasActiveEnrollment(String cpf) {
        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.getStudentCpf().equals(cpf) &&
                    enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lista o histórico de matrículas de um aluno (ativas e canceladas).
     */
    public OperationResult<ArrayList<Enrollment>> listHistoryByStudent(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }

        ArrayList<Enrollment> studentEnrollments = new ArrayList<>();
        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.getStudentCpf().equals(cpf)) {
                studentEnrollments.add(enrollment);
            }
        }

        if (studentEnrollments.isEmpty()) {
            return new OperationResult<>(false, "Nenhuma matrícula encontrada para este aluno.");
        }

        return new OperationResult<>(true,
                studentEnrollments.size() + " matrícula(s) encontrada(s).",
                studentEnrollments);
    }

    /**
     * Registra um novo pagamento para uma matrícula.
     */
    public OperationResult<Payment> registerPayment(
            int enrollmentCode,
            double amount,
            PaymentType paymentType,
            String description,
            String[] paymentData
    ) {
        OperationResult<Void> validationResult = validatePaymentParams(amount, paymentType, paymentData);
        if (!validationResult.isSuccess()) {
            return new OperationResult<>(false, validationResult.getMessage());
        }

        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult<>(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult<>(false, "Não é possível registrar pagamento em matrícula cancelada.");
        }

        Payment payment = createPaymentByType(amount, LocalDateTime.now(), paymentType, description, paymentData);

        enrollment.addPayment(payment);

        double totalPaid = enrollment.getTotalPrice() - enrollment.calculateBalance();
        String message = "✅ Pagamento registrado com sucesso!\n\n" +
                payment.getPaymentSummary() + "\n\n" +
                "Total pago até o momento " + CurrencyFormatter.formatCurrency(totalPaid) + "\n" +
                "Saldo restante " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance());

        return new OperationResult<>(true, message, payment);
    }

    /**
     * Valida os parâmetros de um pagamento.
     * A conversão de {@code amountReceived} para {@code double} é envolvida em
     * {@code try-catch} para relançar {@link NumberFormatException} como
     * {@link InvalidFormatFieldException}.
     */
    private OperationResult<Void> validatePaymentParams(double amount, PaymentType paymentType, String[] paymentData) {
        if (amount <= 0) {
            return new OperationResult<>(false, "O valor do pagamento deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult<>(false, "O tipo de pagamento é obrigatório.");
        }

        if (paymentType == PaymentType.CASH && paymentData != null && paymentData.length > 0) {
            double amountReceived;
            try {
                amountReceived = Double.parseDouble(paymentData[0].replace(",", "."));
            } catch (NumberFormatException e) {
                throw new InvalidFormatFieldException("Valor recebido", "Número decimal (ex.: 99,90)");
            }
            if (amountReceived < amount) {
                return new OperationResult<>(false, "O valor recebido (" + CurrencyFormatter.formatCurrency(amountReceived) +
                        ") deve ser maior ou igual ao valor do pagamento (" + CurrencyFormatter.formatCurrency(amount) + ").");
            }
        }
        return new OperationResult<>(true, "ok");
    }

    /**
     * Lista matrículas que atendem ao critério de um filtro polimórfico.
     */
    public OperationResult<ArrayList<Enrollment>> listByFilter(EnrollmentFilter filter) {
        ArrayList<Enrollment> filtered = new ArrayList<>();
        for (Enrollment enrollment : repository.listAll()) {
            if (filter.matches(enrollment)) {
                filtered.add(enrollment);
            }
        }

        if (filtered.isEmpty()) {
            return new OperationResult<>(false,
                    "Nenhuma matrícula encontrada para o filtro: " + filter.getDescription() + ".");
        }

        return new OperationResult<>(true,
                filtered.size() + " matrícula(s) encontrada(s) — " + filter.getDescription() + ".",
                filtered);
    }

    /**
     * Busca uma matrícula pelo código.
     */
    public Enrollment findByCode(int code) {
        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.getCode() == code) {
                return enrollment;
            }
        }
        return null;
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     */
    public OperationResult<ArrayList<Enrollment>> listAll() {
        if (repository.isEmpty()) {
            return new OperationResult<>(false, "Nenhuma matrícula cadastrada no sistema.");
        }

        return new OperationResult<>(true,
                repository.count() + " matrícula(s) encontrada(s).",
                repository.listAll());
    }

    /**
     * Lista apenas as matrículas ativas.
     */
    public OperationResult<ArrayList<Enrollment>> listActive() {
        ArrayList<Enrollment> activeEnrollments = new ArrayList<>();
        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                activeEnrollments.add(enrollment);
            }
        }

        if (activeEnrollments.isEmpty()) {
            return new OperationResult<>(false, "Nenhuma matrícula ativa encontrada.");
        }

        return new OperationResult<>(true,
                activeEnrollments.size() + " matrícula(s) ativa(s) encontrada(s).",
                activeEnrollments);
    }

    /**
     * Lista apenas as matrículas com saldo pendente.
     */
    public OperationResult<ArrayList<Enrollment>> listWithPendingBalance() {
        ArrayList<Enrollment> pendingEnrollments = new ArrayList<>();
        for (Enrollment enrollment : repository.listAll()) {
            if (enrollment.calculateBalance() > 0) {
                pendingEnrollments.add(enrollment);
            }
        }

        if (pendingEnrollments.isEmpty()) {
            return new OperationResult<>(false, "Nenhuma matrícula com saldo pendente.");
        }

        return new OperationResult<>(true,
                pendingEnrollments.size() + " matrícula(s) com saldo pendente encontrada(s).",
                pendingEnrollments);
    }

    /**
     * Insere uma matrícula totalmente montada (com código, status, cancelledAt
     * e pagamentos já populados) diretamente no repositório, sem aplicar as
     * validações do fluxo de negócio.
     *
     * <p>Uso EXCLUSIVO para mocks de dados de demonstração: permite criar
     * matrículas com datas no passado, históricos retroativos e estados pré-
     * existentes (CANCELLED com cancelledAt arbitrário) que o fluxo regular
     * de {@link #enroll} rejeitaria.</p>
     *
     * <p>Ajusta o contador estático {@code Enrollment.nextCode} se necessário
     * para que matrículas criadas pelo fluxo regular após a chamada não
     * reutilizem o código injetado.</p>
     */
    public void mockEnrollment(Enrollment enrollment) {
        if (enrollment == null) {
            throw new RequiredFieldException("Matrícula");
        }
        repository.add(enrollment);
        if (enrollment.getCode() >= Enrollment.getNextCode()) {
            Enrollment.setNextCode(enrollment.getCode() + 1);
        }
    }
}
