package persistence;

import domain.model.Enrollment;
import domain.model.Student;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PaymentType;
import domain.model.payments.CashPayment;
import domain.model.payments.CreditCardPayment;
import domain.model.payments.DebitCardPayment;
import domain.model.payments.Payment;
import domain.model.payments.PaymentFactory;
import domain.model.payments.PixPayment;
import domain.model.plans.Plan;
import exceptions.CorruptedFileException;
import exceptions.PersistenceException;
import exceptions.WriteFailureException;
import util.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Repositório concreto para matrículas.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Enrollment>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Enrollment} em arquivo texto.
 *
 * Particularidades desta entidade tratadas pelo repositório:
 *  - A coleção referencia objetos {@code Plan} (heterogêneo) e pagamentos
 *    {@code Payment} (também heterogêneo). Ambos os tipos polimórficos
 *    têm o tipo concreto preservado pela presença de um campo {@code type}
 *    no arquivo, usado pela {@link PaymentFactory} ao reconstruir.
 *  - O contador estático {@code Enrollment.nextCode} é persistido como
 *    diretiva especial {@code # nextCode=N} no início do arquivo de
 *    matrículas e restaurado via {@link Enrollment#setNextCode(int)} na
 *    leitura — assim a próxima matrícula não reutiliza códigos entre sessões.
 *  - As referências para {@link Plan} são resolvidas via {@link PlanRepository}
 *    injetado por {@link #setPlanRepository(PlanRepository)}, exigindo que
 *    os planos estejam carregados antes das matrículas.
 *
 * Usado por composição em {@code EnrollmentService}.
 *
 * Arquivos:
 *  - {@code data/enrollments.txt} — matrículas (incluindo diretiva de nextCode).
 *    Formato: {@code code;studentCpf;planName;startDate;durationMonths;totalPrice;status;cancelledAt}
 *  - {@code data/payments.txt} — pagamentos com referência ao código da matrícula.
 *    Formato: {@code enrollmentCode;type;amount;paymentDate;description;extra1;extra2}
 */
public class EnrollmentRepository extends Repository<Enrollment> {

    private static final String ENROLLMENTS_FILE = "enrollments.txt";
    private static final String PAYMENTS_FILE = "payments.txt";
    private static final String ENROLLMENTS_HEADER =
            "# FitManager — Matrículas\n# code;studentCpf;planName;startDate;durationMonths;totalPrice;status;cancelledAt";
    private static final String PAYMENTS_HEADER =
            "# FitManager — Pagamentos\n# enrollmentCode;type;amount;paymentDate;description;extra1;extra2";
    private static final String NEXT_CODE_PREFIX = "# nextCode=";

    private StudentRepository studentRepository;
    private PlanRepository planRepository;

    /**
     * Conecta o repositório de alunos para validação dos CPFs durante o
     * carregamento. Deve ser chamado antes de {@link #load()}.
     */
    public void setStudentRepository(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Conecta o repositório de planos para resolução das referências de
     * {@link Plan} durante o carregamento. Deve ser chamado antes de
     * {@link #load()}.
     */
    public void setPlanRepository(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    /**
     * Persiste matrículas e pagamentos em dois arquivos, incluindo o estado
     * do contador {@code nextCode} e preservando os tipos concretos das
     * subclasses de {@code Plan} (via {@code planName}) e {@code Payment}
     * (via campo {@code type}).
     *
     * @throws PersistenceException se ocorrer falha de escrita em qualquer
     *         um dos dois arquivos
     */
    @Override
    public void save() throws PersistenceException {
        saveEnrollments();
        savePayments();
    }

    /**
     * Recupera matrículas e pagamentos a partir dos arquivos correspondentes,
     * restaurando o contador {@code nextCode} e reconstituindo as referências
     * para alunos, planos e pagamentos com seus tipos concretos.
     * Requer que {@link #setPlanRepository(PlanRepository)} já tenha sido
     * chamado e que o repositório de planos esteja carregado.
     *
     * @throws PersistenceException se algum arquivo estiver corrompido ou
     *         se ocorrer falha de leitura
     */
    @Override
    public void load() throws PersistenceException {
        loadEnrollments();
        loadPayments();
    }

    // ============================
    // Escrita
    // ============================

    private void saveEnrollments() throws PersistenceException {
        Path path = enrollmentsPath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(ENROLLMENTS_HEADER);
            writer.newLine();
            writer.write(NEXT_CODE_PREFIX + Enrollment.getNextCode());
            writer.newLine();
            for (Enrollment enrollment : items) {
                writer.write(serializeEnrollment(enrollment));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
    }

    private void savePayments() throws PersistenceException {
        Path path = paymentsPath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(PAYMENTS_HEADER);
            writer.newLine();
            for (Enrollment enrollment : items) {
                for (Payment payment : enrollment.getPayments()) {
                    writer.write(serializePayment(enrollment.getCode(), payment));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
    }

    private String serializeEnrollment(Enrollment e) {
        return e.getCode() + ";"
                + e.getStudentCpf() + ";"
                + escape(e.getPlanName()) + ";"
                + DateFormatter.formatDate(e.getStartDate()) + ";"
                + e.getDurationMonths() + ";"
                + e.getTotalPrice() + ";"
                + e.getStatus().name() + ";"
                + (e.getCancelledAt() == null ? "" : DateFormatter.formatDate(e.getCancelledAt()));
    }

    private String serializePayment(int enrollmentCode, Payment p) {
        StringBuilder sb = new StringBuilder();
        sb.append(enrollmentCode).append(';')
          .append(p.getPaymentType().name()).append(';')
          .append(p.getAmount()).append(';')
          .append(DateFormatter.formatDateTime(p.getPaymentDate())).append(';')
          .append(escape(p.getDescription())).append(';');
        // Campos específicos da subclasse:
        if (p instanceof PixPayment pix) {
            sb.append(escape(pix.getPixKey())).append(';');
        } else if (p instanceof CreditCardPayment cc) {
            sb.append(cc.getInstallments()).append(';').append(cc.getCardLastDigits());
        } else if (p instanceof DebitCardPayment dc) {
            sb.append(dc.getCardLastDigits()).append(';');
        } else if (p instanceof CashPayment cash) {
            sb.append(cash.getAmountReceived()).append(';');
        } else {
            sb.append(';');
        }
        return sb.toString();
    }

    // ============================
    // Leitura
    // ============================

    private void loadEnrollments() throws PersistenceException {
        Path path = enrollmentsPath();
        if (!Files.exists(path)) {
            return;
        }
        items.clear();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;
                if (trimmed.startsWith(NEXT_CODE_PREFIX)) {
                    try {
                        int nextCode = Integer.parseInt(trimmed.substring(NEXT_CODE_PREFIX.length()).trim());
                        Enrollment.setNextCode(nextCode);
                    } catch (NumberFormatException e) {
                        throw new CorruptedFileException(path.toString(),
                                "Valor de nextCode inválido na linha " + lineNumber, e);
                    }
                    continue;
                }
                if (trimmed.startsWith("#")) continue;
                Enrollment enrollment = deserializeEnrollment(trimmed, lineNumber);
                if (studentRepository != null && studentRepository.findByCpf(enrollment.getStudentCpf()) == null) {
                    throw new CorruptedFileException(enrollmentsPath().toString(),
                            "aluno com CPF " + enrollment.getStudentCpf() + " referenciado na linha " + lineNumber +
                                    " não encontrado no arquivo de alunos");
                }
                items.add(enrollment);
            }
        } catch (IOException e) {
            throw new CorruptedFileException(path.toString(), "erro de leitura", e);
        }
    }

    private void loadPayments() throws PersistenceException {
        Path path = paymentsPath();
        if (!Files.exists(path)) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                applyPayment(trimmed, lineNumber);
            }
        } catch (IOException e) {
            throw new CorruptedFileException(path.toString(), "erro de leitura", e);
        }
    }

    private Enrollment deserializeEnrollment(String line, int lineNumber) throws CorruptedFileException {
        String[] parts = line.split(";", -1);
        if (parts.length < 7) {
            throw new CorruptedFileException(enrollmentsPath().toString(),
                    "linha " + lineNumber + " com número insuficiente de campos");
        }
        try {
            int code = Integer.parseInt(parts[0]);
            String studentCpf = parts[1];
            String planName = parts[2];
            LocalDate startDate = DateFormatter.parseDate(parts[3]);
            int durationMonths = Integer.parseInt(parts[4]);
            double totalPrice = Double.parseDouble(parts[5]);
            EnrollmentStatus status = EnrollmentStatus.valueOf(parts[6]);
            LocalDate cancelledAt = (parts.length > 7 && !parts[7].isEmpty())
                    ? DateFormatter.parseDate(parts[7]) : null;

            Plan plan = resolvePlan(planName, lineNumber);
            return new Enrollment(code, studentCpf, plan, startDate, durationMonths,
                    totalPrice, status, cancelledAt);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new CorruptedFileException(enrollmentsPath().toString(),
                    "valor inválido na linha " + lineNumber, e);
        }
    }

    private Plan resolvePlan(String planName, int lineNumber) throws CorruptedFileException {
        if (planRepository == null) {
            throw new CorruptedFileException(enrollmentsPath().toString(),
                    "PlanRepository não conectado — não é possível resolver plano \"" + planName + "\"");
        }
        for (Plan plan : planRepository.listAll()) {
            if (plan.getName().equalsIgnoreCase(planName)) {
                return plan;
            }
        }
        throw new CorruptedFileException(enrollmentsPath().toString(),
                "plano \"" + planName + "\" referenciado na linha " + lineNumber +
                        " não encontrado no arquivo de planos");
    }

    private void applyPayment(String line, int lineNumber) throws CorruptedFileException {
        String[] parts = line.split(";", -1);
        if (parts.length < 5) {
            throw new CorruptedFileException(paymentsPath().toString(),
                    "linha " + lineNumber + " com número insuficiente de campos");
        }
        try {
            int enrollmentCode = Integer.parseInt(parts[0]);
            PaymentType type = PaymentType.valueOf(parts[1]);
            double amount = Double.parseDouble(parts[2]);
            LocalDateTime paymentDate = DateFormatter.parseDateTime(parts[3]);
            String description = parts[4];

            String[] extra = extractExtraFields(type, parts);
            Payment payment = PaymentFactory.create(type, amount, paymentDate, description, extra);

            Enrollment target = findByCode(enrollmentCode);
            if (target == null) {
                throw new CorruptedFileException(paymentsPath().toString(),
                        "pagamento na linha " + lineNumber + " referencia matrícula inexistente: " + enrollmentCode);
            }
            target.addPayment(payment);
        } catch (IllegalArgumentException | DateTimeParseException e) {
            throw new CorruptedFileException(paymentsPath().toString(),
                    "valor inválido na linha " + lineNumber, e);
        }
    }

    /**
     * Extrai os campos específicos da subclasse de pagamento a partir das
     * partes adicionais da linha CSV.
     */
    private String[] extractExtraFields(PaymentType type, String[] parts) {
        switch (type) {
            case PIX:
                return new String[]{parts.length > 5 ? parts[5] : ""};
            case CREDIT_CARD:
                String installments = parts.length > 5 ? parts[5] : "1";
                String creditDigits = parts.length > 6 ? parts[6] : "0000";
                return new String[]{installments, creditDigits};
            case DEBIT_CARD:
                return new String[]{parts.length > 5 ? parts[5] : "0000"};
            case CASH:
                return new String[]{parts.length > 5 ? parts[5] : "0"};
            default:
                return new String[0];
        }
    }

    private Enrollment findByCode(int code) {
        for (Enrollment e : items) {
            if (e.getCode() == code) {
                return e;
            }
        }
        return null;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace(";", ",");
    }

    private Path enrollmentsPath() {
        return Paths.get(DATA_DIR, ENROLLMENTS_FILE);
    }

    private Path paymentsPath() {
        return Paths.get(DATA_DIR, PAYMENTS_FILE);
    }
}
