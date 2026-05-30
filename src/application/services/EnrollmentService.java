package application.services;

import application.OperationResult;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PaymentType;
import domain.model.filters.EnrollmentFilter;
import domain.model.Enrollment;
import domain.model.payments.Payment;
import domain.model.payments.PixPayment;
import domain.model.payments.CashPayment;
import domain.model.payments.CreditCardPayment;
import domain.model.payments.DebitCardPayment;
import domain.model.plans.Plan;
import domain.model.Student;
import exceptions.InvalidFormatFieldException;
import exceptions.RequiredFieldException;
import util.CurrencyFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
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

    private ArrayList<Enrollment> enrollments;

    public EnrollmentService() {
        this.enrollments = new ArrayList<>();
    }

    /**
     * Realiza a matrícula de um aluno em um plano.
     */
    public OperationResult enroll(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {
        OperationResult validationResult = validateEnrollmentParams(
                student, plan, startDate, durationMonths);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        OperationResult paymentValidation = validatePaymentParams(initialAmount, paymentType, paymentData);
        if (!paymentValidation.isSuccess()) {
            return paymentValidation;
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

        enrollments.add(enrollment);

        String message = "✅ Matrícula realizada com sucesso!\n\n" +
                "Código: " + enrollment.getCode() + "\n" +
                "Aluno: " + student.getName() + "\n" +
                "Plano: " + plan.getName() + " (" + plan.getTypeName() + ")\n" +
                "Preço Total: " + CurrencyFormatter.formatCurrency(totalPrice) + "\n" +
                "Pagamento Inicial: " + CurrencyFormatter.formatCurrency(initialAmount) + "\n" +
                "Saldo Pendente: " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance()) + "\n\n" +
                initialPayment.getPaymentSummary();

        return new OperationResult(true, message, enrollment);
    }

    /**
     * Valida os parâmetros necessários para uma matrícula.
     * Argumentos nulos são tratados como campos obrigatórios ausentes.
     */
    private OperationResult validateEnrollmentParams(
            Student student,
            Plan plan,
            LocalDate startDate,
            int durationMonths
    ) {
        if (student == null) {
            throw new RequiredFieldException("aluno");
        }
        if (plan == null) {
            throw new RequiredFieldException("plano");
        }
        if (startDate == null) {
            throw new RequiredFieldException("data de início");
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
     * Instancia a subclasse correta de Payment com base no PaymentType.
     * As conversões {@code Integer.parseInt}/{@code Double.parseDouble} são
     * envoltas em {@code try-catch} para que qualquer {@link NumberFormatException}
     * vinda de dados mal formados seja relançada como {@link InvalidFormatFieldException},
     * mantendo a comunicação de falhas dentro da hierarquia do FitManager.
     */
    private Payment createPaymentByType(
            double amount,
            LocalDateTime paymentDate,
            PaymentType paymentType,
            String description,
            String[] paymentData
    ) {
        switch (paymentType) {
            case PIX:
                String pixKey = (paymentData != null && paymentData.length > 0 ? paymentData[0] : "");
                return new PixPayment(amount, paymentDate, description, pixKey);
            case CREDIT_CARD:
                int installments = 1;
                String creditDigits = "0000";
                if (paymentData != null && paymentData.length >= 2) {
                    try {
                        installments = Integer.parseInt(paymentData[0]);
                    } catch (NumberFormatException e) {
                        throw new InvalidFormatFieldException("número de parcelas", "número inteiro");
                    }
                    creditDigits = paymentData[1];
                }
                return new CreditCardPayment(amount, paymentDate, description, installments, creditDigits);
            case DEBIT_CARD:
                String debitDigits = (paymentData != null && paymentData.length > 0 ? paymentData[0] : "0000");
                return new DebitCardPayment(amount, paymentDate, description, debitDigits);
            case CASH:
                double amountReceived = amount;
                if (paymentData != null && paymentData.length > 0) {
                    try {
                        amountReceived = Double.parseDouble(paymentData[0].replace(",", "."));
                    } catch (NumberFormatException e) {
                        throw new InvalidFormatFieldException("valor recebido", "número decimal (ex.: 99,90)");
                    }
                }
                return new CashPayment(amount, paymentDate, description, amountReceived);
            default:
                return new PixPayment(amount, paymentDate, description, "");
        }
    }

    /**
     * Cancela uma matrícula ativa.
     */
    public OperationResult cancelEnrollment(int enrollmentCode) {
        Enrollment enrollment = findByCode(enrollmentCode);
        if (enrollment == null) {
            return new OperationResult(false, "Matrícula não encontrada.");
        }

        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            return new OperationResult(false, "Esta matrícula já foi cancelada.");
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

        return new OperationResult(true, message, enrollment);
    }

    /**
     * Busca a matrícula ativa de um aluno pelo CPF.
     */
    public OperationResult findActiveByStudentCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
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
     */
    public OperationResult listHistoryByStudent(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
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
     */
    public OperationResult registerPayment(
            int enrollmentCode,
            double amount,
            PaymentType paymentType,
            String description,
            String[] paymentData
    ) {
        OperationResult validationResult = validatePaymentParams(amount, paymentType, paymentData);
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

        Payment payment = createPaymentByType(amount, LocalDateTime.now(), paymentType, description, paymentData);

        enrollment.addPayment(payment);

        double totalPaid = enrollment.getTotalPrice() - enrollment.calculateBalance();
        String message = "✅ Pagamento registrado com sucesso!\n\n" +
                payment.getPaymentSummary() + "\n\n" +
                "Total pago até o momento " + CurrencyFormatter.formatCurrency(totalPaid) + "\n" +
                "Saldo restante " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance());

        return new OperationResult(true, message, payment);
    }

    /**
     * Valida os parâmetros de um pagamento.
     * A conversão de {@code amountReceived} para {@code double} é envolvida em
     * {@code try-catch} para relançar {@link NumberFormatException} como
     * {@link InvalidFormatFieldException}.
     */
    private OperationResult validatePaymentParams(double amount, PaymentType paymentType, String[] paymentData) {
        if (amount <= 0) {
            return new OperationResult(false, "O valor do pagamento deve ser positivo.");
        }
        if (paymentType == null) {
            return new OperationResult(false, "O tipo de pagamento é obrigatório.");
        }

        if (paymentType == PaymentType.CASH && paymentData != null && paymentData.length > 0) {
            double amountReceived;
            try {
                amountReceived = Double.parseDouble(paymentData[0].replace(",", "."));
            } catch (NumberFormatException e) {
                throw new InvalidFormatFieldException("valor recebido", "número decimal (ex.: 99,90)");
            }
            if (amountReceived < amount) {
                return new OperationResult(false, "O valor recebido (" + CurrencyFormatter.formatCurrency(amountReceived) +
                        ") deve ser maior ou igual ao valor do pagamento (" + CurrencyFormatter.formatCurrency(amount) + ").");
            }
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Lista matrículas que atendem ao critério de um filtro polimórfico.
     */
    public OperationResult listByFilter(EnrollmentFilter filter) {
        ArrayList<Enrollment> filtered = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (filter.matches(enrollment)) {
                filtered.add(enrollment);
            }
        }

        if (filtered.isEmpty()) {
            return new OperationResult(false,
                    "Nenhuma matrícula encontrada para o filtro: " + filter.getDescription() + ".");
        }

        return new OperationResult(true,
                filtered.size() + " matrícula(s) encontrada(s) — " + filter.getDescription() + ".",
                filtered);
    }

    /**
     * Busca uma matrícula pelo código.
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
     * Lista apenas as matrículas com saldo pendente.
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
