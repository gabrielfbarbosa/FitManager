package application.services;

import application.OperationResult;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PaymentType;
import domain.model.Enrollment;
import domain.model.Payment;
import domain.model.plans.Plan;
import domain.model.Student;
import util.CurrencyFormatter;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
 */
public class EnrollmentService {

    private ArrayList<Enrollment> enrollments;

    public EnrollmentService() {
        this.enrollments = new ArrayList<>();
    }

    /**
     * Realiza a matrícula de um aluno em um plano.
     * Cria um Enrollment e registra um pagamento inicial.
     *
     * @return OperationResult com o Enrollment criado em data (se sucesso)
     */
    public OperationResult enroll(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription
    ) {
        OperationResult validationResult = validateEnrollmentParams(
                student, plan, startDate, durationMonths);
        if (!validationResult.isSuccess()) {
            return validationResult;
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
        Payment initialPayment = buildPayment(
                initialAmount,
                LocalDate.now(),
                paymentType,
                paymentDescription
        );
        enrollment.addPayment(initialPayment);

        enrollments.add(enrollment);

        String message = "✅ Matrícula realizada com sucesso!\n\n" +
                "Código: " + enrollment.getCode() + "\n" +
                "Aluno: " + student.getName() + "\n" +
                "Plano: " + plan.getName() + "\n" +
                "Preço Total: " + CurrencyFormatter.formatCurrency(totalPrice) + "\n" +
                "Pagamento Inicial: " + CurrencyFormatter.formatCurrency(initialAmount) + "\n" +
                "Saldo Pendente: " + CurrencyFormatter.formatCurrency(totalPrice - initialAmount);

        return new OperationResult(true, message, enrollment);
    }

    /**
     * Valida os parâmetros necessários para uma matrícula.
     */
    private OperationResult validateEnrollmentParams(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths
    ) {
        if (student == null) {
            return new OperationResult(false, "Aluno não pode ser nulo.");
        }
        if (plan == null) {
            return new OperationResult(false, "Plano não pode ser nulo.");
        }
        if (startDate == null) {
            return new OperationResult(false, "Data de início não pode ser nula.");
        }
        if (startDate.isBefore(LocalDate.now())) {
            return new OperationResult(false, "A data de início não pode ser anterior a hoje.");
        }
        if (durationMonths < plan.getMinimumDuration()) {
            return new OperationResult(false,
                    "A duração deve ser no mínimo " + plan.getMinimumDuration() +
                            (plan.getMinimumDuration() == 1 ? " mês" : " meses") + ".");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Cria um objeto Payment com os parâmetros fornecidos.
     * Usado durante a matrícula para criar o pagamento inicial.
     */
    private Payment buildPayment(
            double amount,
            LocalDate paymentDate,
            PaymentType paymentType,
            String description
    ) {
        return new Payment(amount, paymentDate, paymentType, description);
    }

    /**
     * Cancela uma matrícula ativa.
     * Calcula a taxa de cancelamento via polimorfismo (plan.getCancellationFee)
     * ANTES de efetuar o cancelamento, e inclui o resumo financeiro no resultado.
     *
     * @return OperationResult com o Enrollment em data (se sucesso)
     */
    public OperationResult cancelEnrollment(int enrollmentCode) {
        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult(false, "Esta matrícula já foi cancelada.");
        }

        // Calcula taxa de cancelamento ANTES de cancelar (polimorfismo)
        double cancellationFee = enrollment.getPlan().getCancellationFee(enrollment);

        // Coleta informações financeiras antes do cancelamento
        double totalPrice = enrollment.getTotalPrice();
        double totalPaid = totalPrice - enrollment.calculateBalance();
        double pendingBalance = enrollment.calculateBalance();

        // Efetua o cancelamento
        enrollment.cancel();

        // Monta resumo financeiro do cancelamento
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

        return new OperationResult(true, message, enrollment);
    }

    /**
     * Busca a matrícula ativa de um aluno pelo CPF.
     *
     * @return OperationResult com o Enrollment encontrado em data (se sucesso)
     */
    public OperationResult findActiveByStudentCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentCpf().equals(cpf) &&
                    enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return new OperationResult(true, "Matrícula encontrada.", enrollment);
            }
        }

        return new OperationResult(false, "Nenhuma matrícula ativa encontrada para este aluno.");
    }

    /**
     * Verifica se um aluno possui matrícula ativa.
     * Usado pelo FitManager para validar remoção de alunos e nova matrícula.
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return true se o aluno possui ao menos uma matrícula ativa
     */
    public boolean hasActiveEnrollment(String cpf) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentCpf().equals(cpf) &&
                    enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lista o histórico de matrículas de um aluno (ativas e canceladas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listHistoryByStudent(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        ArrayList<Enrollment> studentEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStudentCpf().equals(cpf)) {
                studentEnrollments.add(enrollment);
            }
        }

        if (studentEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula encontrada para este aluno.");
        }

        return new OperationResult(true,
                studentEnrollments.size() + " matrícula(s) encontrada(s).",
                studentEnrollments);
    }

    /**
     * Registra um novo pagamento para uma matrícula.
     * O pagamento é validado e então adicionado à matrícula.
     *
     * @return OperationResult indicando sucesso ou falha
     */
    public OperationResult registerPayment(
            int enrollmentCode,
            double amount,
            PaymentType paymentType,
            String description
    ) {
        OperationResult validationResult = validatePaymentParams(amount, paymentType);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult(false, "Não é possível registrar pagamento em matrícula cancelada.");
        }

        double remainingBalance = enrollment.calculateBalance();
        if (amount > remainingBalance) {
            return new OperationResult(false,
                    "O valor do pagamento (" + CurrencyFormatter.formatCurrency(amount) +
                            ") excede o saldo pendente (" + CurrencyFormatter.formatCurrency(remainingBalance) + ").");
        }

        Payment payment = buildPayment(amount, LocalDate.now(), paymentType, description);
        enrollment.addPayment(payment);

        String message = "✅ Pagamento registrado com sucesso!\n\n" +
                "Código do Pagamento: " + payment.getCode() + "\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(amount) + "\n" +
                "Tipo: " + paymentType.getLabel() + "\n" +
                "Novo Saldo Pendente: " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance());

        return new OperationResult(true, message, payment);
    }

    /**
     * Valida os parâmetros de um pagamento.
     */
    private OperationResult validatePaymentParams(double amount, PaymentType paymentType) {
        if (amount <= 0) {
            return new OperationResult(false, "O valor do pagamento deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Busca uma matrícula pelo código.
     * Utilizado internamente para validações e operações.
     */
    public Enrollment findByCode(int code) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCode() == code) {
                return enrollment;
            }
        }
        return null;
    }

    /**
     * Lista todas as matrículas (ativas e canceladas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listAll() {
        if (enrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula cadastrada no sistema.");
        }

        return new OperationResult(true,
                enrollments.size() + " matrícula(s) encontrada(s).",
                new ArrayList<>(enrollments));
    }

    /**
     * Lista apenas as matrículas ativas.
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listActive() {
        ArrayList<Enrollment> activeEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getStatus() == EnrollmentStatus.ACTIVE) {
                activeEnrollments.add(enrollment);
            }
        }

        if (activeEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula ativa encontrada.");
        }

        return new OperationResult(true,
                activeEnrollments.size() + " matrícula(s) ativa(s) encontrada(s).",
                activeEnrollments);
    }

    /**
     * Lista apenas as matrículas que possuem saldo pendente (não estão totalmente pagas).
     *
     * @return OperationResult com ArrayList<Enrollment> em data
     */
    public OperationResult listWithPendingBalance() {
        ArrayList<Enrollment> pendingEnrollments = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (enrollment.calculateBalance() > 0) {
                pendingEnrollments.add(enrollment);
            }
        }

        if (pendingEnrollments.isEmpty()) {
            return new OperationResult(false, "Nenhuma matrícula com saldo pendente.");
        }

        return new OperationResult(true,
                pendingEnrollments.size() + " matrícula(s) com saldo pendente encontrada(s).",
                pendingEnrollments);
    }
}