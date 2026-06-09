package mocks;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import domain.model.enums.EnrollmentStatus;
import domain.model.enums.PaymentType;
import domain.model.enums.PlanType;
import domain.model.payments.Payment;
import domain.model.payments.PaymentFactory;
import domain.model.plans.Plan;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Popula o sistema com um volume grande de dados realistas para testar
 * todos os relatórios disponíveis em ReportsMenu.
 *
 * <h2>Como criar matrículas com datas no passado</h2>
 *
 * O fluxo de negócio do {@link FitManager} rejeita datas anteriores a hoje
 * — política correta para entrada de dados em produção. Para o mock de
 * demonstração, montamos as matrículas diretamente via o construtor de
 * restauração de {@link Enrollment} (mesmo usado pela camada de persistência)
 * e a inserimos no sistema pela única porta mock-only exposta:
 * {@link FitManager#mockEnrollment(Enrollment)}.
 *
 * Pagamentos com data retroativa são criados via {@link PaymentFactory#create}
 * e anexados antes do mock através de {@link Enrollment#addPayment(Payment)},
 * que já é parte do contrato público da entidade.
 *
 * <h2>Cenários cobertos</h2>
 *  1. LISTAR_ALUNOS           → 20 alunos (17 ativos + 3 inativos)
 *  2. LISTAR_PLANOS           → 4 planos (Mensal, Trimestral, Semestral, Anual)
 *  3. LISTAR_MATRICULAS       → ~25 matrículas (ativas + canceladas)
 *  4. MATRICULAS_ATIVAS       → ~17 matrículas ativas
 *  5. MATRICULAS_CANCELADAS   → ~6 matrículas canceladas (em meses diferentes)
 *  6. SALDO_PENDENTE          → matrículas com pagamento parcial ou apenas entrada
 *  7. POR_TIPO_PLANO          → matrículas distribuídas nos 4 tipos de plano
 *  8. VENCIDAS                → matrículas ativas com endDate anterior a hoje
 *  9. CONSULTAR_ALUNO         → qualquer CPF da lista de alunos
 * 10. CONSULTAR_PLANO         → qualquer nome de plano
 * 11. CONSULTAR_MATRICULA     → alunos com matrícula ativa consultáveis por CPF
 * 12. ESTATISTICAS            → derivados de todas as entidades acima
 * 13. RELATORIO_FINANCEIRO    → pagamentos espalhados por mês (março–agosto 2026)
 */
public class DataMock {

    // Data de referência: hoje
    private static final LocalDate TODAY = LocalDate.now();

    public static void populateDemo(FitManager fm) {
        mockPlans(fm);
        mockStudents(fm);
        mockEnrollmentsAndPayments(fm);
        mockCancelledEnrollments(fm);
        mockInactiveStudents(fm);
    }

    // ============================
    // Planos (4 tipos)
    // ============================

    private static void mockPlans(FitManager fm) {
        fm.registerPlan(
                "Plano Mensal",
                "Acesso à academia por 1 mês, renovável mensalmente.",
                PlanType.MONTHLY,
                1,
                99.90
        );

        fm.registerPlan(
                "Plano Trimestral",
                "Acesso à academia por trimestre com desconto progressivo.",
                PlanType.QUARTERLY,
                3,
                89.90
        );

        fm.registerPlan(
                "Plano Semestral",
                "Acesso à academia por semestre com desconto progressivo.",
                PlanType.SEMI_ANNUAL,
                6,
                79.90
        );

        fm.registerPlan(
                "Plano Anual",
                "Acesso à academia por um ano inteiro com melhor custo-benefício.",
                PlanType.ANNUAL,
                12,
                69.90
        );
    }

    // ============================
    // Alunos (20 alunos)
    // ============================

    private static void mockStudents(FitManager fm) {
        // ---- 17 alunos que permanecerão ativos ----
        fm.registerStudent("Carlos Eduardo Silva",   "52998224725", "carlos.silva@email.com",    "15/03/1995");
        fm.registerStudent("Ana Paula Ferreira",     "71428793860", "ana.ferreira@email.com",    "22/07/1998");
        fm.registerStudent("Bruno Henrique Costa",   "87748248800", "bruno.costa@gmail.com",     "08/11/1990");
        fm.registerStudent("Fernanda Lima Rocha",    "34650463238", "fernanda.rocha@email.com",  "30/01/2000");
        fm.registerStudent("Patrícia Oliveira Santos","12345678909","patricia.santos@email.com", "25/05/1997");
        fm.registerStudent("Lucas Rodrigues Ferreira","98765432100","(67) 99234-5678",           "10/02/1994");
        fm.registerStudent("Mariana Beatriz Campos", "11122233396", "mariana.campos@gmail.com",  "17/09/2001");
        fm.registerStudent("Letícia Aparecida Moura","23456789092", "leticia.moura@email.com",   "12/11/1999");
        fm.registerStudent("Rafael Nunes Pereira",   "84887172370", "rafael.nunes@email.com",    "05/04/1990");
        fm.registerStudent("Camila Rocha Dias",      "72842768000", "camila.rocha@email.com",    "22/01/2001");
        fm.registerStudent("Eduardo Lima Martins",   "72624506462", "eduardo.lima@email.com",    "30/11/1992");
        fm.registerStudent("Hugo Martins Almeida",   "77208349258", "hugo.martins@email.com",    "25/02/1993");
        fm.registerStudent("Gisele Nogueira Souza",  "93239666499", "gisele.nogueira@email.com", "10/06/1996");
        fm.registerStudent("Isabela Castro Mendes",  "76513005213", "isabela.castro@email.com",  "19/08/2000");
        fm.registerStudent("Fábio Dias Oliveira",    "04798237825", "fabio.dias@email.com",      "14/09/1998");
        fm.registerStudent("Renata Vieira Costa",    "65187321056","renata.vieira@email.com",   "08/03/1996");
        fm.registerStudent("Thiago Nascimento Gomes","44455566619", "(67) 98321-4567",           "03/07/1988");

        // ---- Novos alunos para o mes 03/2026 e matrículas vencidas ----
        fm.registerStudent("Rodrigo Alves",          "83274619078", "rodrigo.alves@email.com",   "10/10/1990");
        fm.registerStudent("Amanda Martins",         "29384710253", "amanda.martins@email.com",  "12/05/1994");
        fm.registerStudent("Ricardo Santos",         "48201938488", "ricardo.santos@email.com",  "20/08/1987");
        fm.registerStudent("Priscila Lima",          "10293847541", "priscila.lima@email.com",   "05/12/1993");

        // ---- 3 alunos que serão inativados depois ----
        fm.registerStudent("Juliana Torres Souza",   "07859546434", "ju.torres@email.com",       "03/09/1993");
        fm.registerStudent("Marcos Vinicius Lima",   "18345678904", "marcos.lima@email.com",     "11/12/1992");
        fm.registerStudent("Diego Henrique Prado",   "77788899941", "diego.prado@email.com",     "29/04/1991");
    }

    // ============================
    // Matrículas e Pagamentos
    // ============================

    private static void mockEnrollmentsAndPayments(FitManager fm) {

        String todayStr = formatDate(TODAY);
        String tomorrowStr = formatDate(TODAY.plusDays(1));
        String nextWeekStr = formatDate(TODAY.plusDays(7));

        // =====================================================
        // MATRÍCULAS COM PLANO ANUAL (3 alunos)
        // =====================================================

        // 1. Carlos — Plano Anual / ativa / parcialmente paga / pagamentos março–junho
        OperationResult<Enrollment> e1 = fm.enrollStudent(
                "52998224725", "Plano Anual", todayStr, 12,
                69.90, PaymentType.PIX, "Parcela 1 — matrícula",
                new String[]{"carlos.silva@email.com"}
        );
        if (e1.isSuccess()) {
            Enrollment enr = e1.getData();
            addPixPayment(enr, 69.90, 5, 4, 2026, "Parcela 2 — abril");
            addPixPayment(enr, 69.90, 5, 5, 2026, "Parcela 3 — maio");
            addDebitPayment(enr, 69.90, 3, 6, 2026, "Parcela 4 — junho");
        }

        // 2. Mariana — Plano Anual / ativa / 2 parcelas pagas (saldo pendente alto)
        OperationResult<Enrollment> e2 = fm.enrollStudent(
                "11122233396", "Plano Anual", todayStr, 12,
                69.90, PaymentType.DEBIT_CARD, "Parcela 1",
                new String[]{"4321"}
        );
        if (e2.isSuccess()) {
            addDebitPayment(e2.getData(), 69.90, 10, 5, 2026, "Parcela 2 — maio");
        }

        // 3. Camila — Plano Anual / ativa / 3 parcelas pagas em meses diferentes
        OperationResult<Enrollment> e3 = fm.enrollStudent(
                "72842768000", "Plano Anual", todayStr, 12,
                69.90, PaymentType.PIX, "Parcela 1",
                new String[]{"camila.rocha@email.com"}
        );
        if (e3.isSuccess()) {
            Enrollment enr = e3.getData();
            addPixPayment(enr, 69.90, 5, 4, 2026, "Parcela 2 — abril");
            addCreditPayment(enr, 69.90, 5, 5, 2026, "Parcela 3 — maio");
        }

        // =====================================================
        // MATRÍCULAS COM PLANO SEMESTRAL (4 alunos)
        // =====================================================

        // 4. Patrícia — Plano Semestral / ativa / 4 de 6 parcelas pagas
        OperationResult<Enrollment> e4 = fm.enrollStudent(
                "12345678909", "Plano Semestral", todayStr, 6,
                79.90, PaymentType.PIX, "Parcela 1 — matrícula",
                new String[]{"patricia.santos@email.com"}
        );
        if (e4.isSuccess()) {
            Enrollment enr = e4.getData();
            addPixPayment(enr, 79.90, 1, 4, 2026, "Parcela 2 — abril");
            addPixPayment(enr, 79.90, 1, 5, 2026, "Parcela 3 — maio");
            addDebitPayment(enr, 79.90, 1, 6, 2026, "Parcela 4 — junho");
        }

        // 5. Hugo — Plano Semestral / ativa / 3 parcelas com cartão de crédito
        OperationResult<Enrollment> e5 = fm.enrollStudent(
                "77208349258", "Plano Semestral", todayStr, 6,
                79.90, PaymentType.CREDIT_CARD, "Parcela 1",
                new String[]{"1", "5678"}
        );
        if (e5.isSuccess()) {
            Enrollment enr = e5.getData();
            addCreditPayment(enr, 79.90, 3, 5, 2026, "Parcela 2 — maio");
            addCreditPayment(enr, 79.90, 3, 6, 2026, "Parcela 3 — junho");
        }

        // 6. Isabela — Plano Semestral / ativa / apenas pagamento inicial (saldo pendente)
        fm.enrollStudent(
                "76513005213", "Plano Semestral", todayStr, 6,
                79.90, PaymentType.CASH, "Pagamento inicial",
                new String[]{"80.00"}
        );

        // 7. Eduardo — Plano Semestral / ativa / pagamento com dinheiro + PIX
        OperationResult<Enrollment> e7 = fm.enrollStudent(
                "72624506462", "Plano Semestral", tomorrowStr, 6,
                79.90, PaymentType.CASH, "Parcela 1 em dinheiro",
                new String[]{"80.00"}
        );
        if (e7.isSuccess()) {
            Enrollment enr = e7.getData();
            addPixPayment(enr, 79.90, 15, 5, 2026, "Parcela 2 — PIX");
            addCashPayment(enr, 79.90, 15, 6, 2026, "Parcela 3 — dinheiro");
        }

        // =====================================================
        // MATRÍCULAS COM PLANO TRIMESTRAL (5 alunos)
        // =====================================================

        // 8. Bruno — Plano Trimestral / ativa / totalmente quitada
        OperationResult<Enrollment> e8 = fm.enrollStudent(
                "87748248800", "Plano Trimestral", todayStr, 3,
                89.90, PaymentType.CREDIT_CARD, "Parcela 1",
                new String[]{"1", "5678"}
        );
        if (e8.isSuccess()) {
            Enrollment enr = e8.getData();
            addCreditPayment(enr, 89.90, 3, 4, 2026, "Parcela 2 — abril");
            addCreditPayment(enr, 89.90, 3, 5, 2026, "Parcela 3 — quitado");
        }

        // 9. Rafael — Plano Trimestral / ativa / apenas entrada (saldo pendente alto)
        fm.enrollStudent(
                "84887172370", "Plano Trimestral", todayStr, 3,
                50.00, PaymentType.CASH, "Entrada parcial",
                new String[]{"50.00"}
        );

        // 10. Gisele — Plano Trimestral / ativa / 2 parcelas pagas via PIX
        OperationResult<Enrollment> e10 = fm.enrollStudent(
                "93239666499", "Plano Trimestral", tomorrowStr, 3,
                89.90, PaymentType.PIX, "Parcela 1",
                new String[]{"gisele.nogueira@email.com"}
        );
        if (e10.isSuccess()) {
            addPixPayment(e10.getData(), 89.90, 20, 5, 2026, "Parcela 2 — maio");
        }

        // 11. Fábio — Plano Trimestral / ativa / parcialmente paga via débito
        OperationResult<Enrollment> e11 = fm.enrollStudent(
                "04798237825", "Plano Trimestral", tomorrowStr, 3,
                89.90, PaymentType.DEBIT_CARD, "Parcela 1",
                new String[]{"4321"}
        );
        if (e11.isSuccess()) {
            addDebitPayment(e11.getData(), 89.90, 25, 5, 2026, "Parcela 2 — maio");
        }

        // 12. Thiago — Plano Trimestral / ativa / 1 parcela paga
        fm.enrollStudent(
                "44455566619", "Plano Trimestral", nextWeekStr, 3,
                89.90, PaymentType.PIX, "Parcela 1",
                new String[]{"thiago@email.com"}
        );

        // =====================================================
        // MATRÍCULAS COM PLANO MENSAL (4 alunos)
        // =====================================================

        // 13. Ana Paula — Plano Mensal / ativa / quitada
        fm.enrollStudent(
                "71428793860", "Plano Mensal", todayStr, 1,
                99.90, PaymentType.PIX, "Pagamento integral",
                new String[]{"ana.ferreira@email.com"}
        );

        // 14. Lucas — Plano Mensal / ativa / totalmente quitada via cartão de crédito
        fm.enrollStudent(
                "98765432100", "Plano Mensal", todayStr, 1,
                99.90, PaymentType.CREDIT_CARD, "Pagamento integral",
                new String[]{"1", "5678"}
        );

        // 15. Letícia — Plano Mensal / ativa / pagamento parcial (saldo pendente)
        fm.enrollStudent(
                "23456789092", "Plano Mensal", tomorrowStr, 1,
                50.00, PaymentType.CASH, "Entrada parcial",
                new String[]{"50.00"}
        );

        // 16. Fernanda — Plano Mensal / ativa / quitada via débito
        fm.enrollStudent(
                "34650463238", "Plano Mensal", tomorrowStr, 1,
                99.90, PaymentType.DEBIT_CARD, "Pagamento integral",
                new String[]{"4321"}
        );

        // =====================================================
        // MATRÍCULAS DE MARÇO DE 2026 (datas no passado via mock)
        // =====================================================

        // 17. Renata — Plano Mensal / ativa / vencida (início 01/03/2026, fim 31/03/2026)
        mockActive(fm, "65187321056", "Plano Mensal",
                LocalDate.of(2026, 3, 1), 1,
                99.90, PaymentType.CASH, "Pagamento integral em dinheiro",
                new String[]{"100.00"});

        // 18. Rodrigo — Plano Trimestral / ativa / quitada (início 10/03/2026)
        Enrollment rodrigo = mockActive(fm, "83274619078", "Plano Trimestral",
                LocalDate.of(2026, 3, 10), 3,
                89.90, PaymentType.PIX, "Parcela 1 — PIX",
                new String[]{"rodrigo.alves@email.com"});
        if (rodrigo != null) {
            addPixPayment(rodrigo, 89.90, 10, 4, 2026, "Parcela 2 — abril");
            addPixPayment(rodrigo, 89.90, 10, 5, 2026, "Parcela 3 — quitado");
        }

        // 19. Amanda — Plano Semestral / ativa / saldo pendente (início 15/03/2026)
        Enrollment amanda = mockActive(fm, "29384710253", "Plano Semestral",
                LocalDate.of(2026, 3, 15), 6,
                79.90, PaymentType.CREDIT_CARD, "Parcela 1 — cartão",
                new String[]{"1", "5678"});
        if (amanda != null) {
            addCreditPayment(amanda, 79.90, 15, 4, 2026, "Parcela 2 — abril");
            addCreditPayment(amanda, 79.90, 15, 5, 2026, "Parcela 3 — maio");
        }

        // 20. Ricardo — Plano Anual / ativa / saldo pendente (início 05/03/2026)
        mockActive(fm, "48201938488", "Plano Anual",
                LocalDate.of(2026, 3, 5), 12,
                69.90, PaymentType.DEBIT_CARD, "Parcela 1 — débito",
                new String[]{"4321"});

        // 21. Priscila — Plano Mensal / iniciada em março e cancelada em março
        mockCancelled(fm, "10293847541", "Plano Mensal",
                LocalDate.of(2026, 3, 12), 1,
                LocalDate.of(2026, 3, 22),
                99.90, PaymentType.PIX, "Matrícula via PIX",
                new String[]{"priscila.lima@email.com"});
    }

    // ============================
    // Matrículas Canceladas (com datas históricas via mock)
    // ============================

    private static void mockCancelledEnrollments(FitManager fm) {

        // ---- Juliana: matrícula cancelada em março ----
        mockCancelled(fm, "07859546434", "Plano Mensal",
                LocalDate.of(2026, 3, 1), 1,
                LocalDate.of(2026, 3, 20),
                99.90, PaymentType.PIX, "Pagamento integral",
                new String[]{"ju.torres@email.com"});

        // ---- Marcos: matrícula cancelada em abril (com 2 pagamentos) ----
        Enrollment marcos = mockCancelled(fm, "18345678904", "Plano Trimestral",
                LocalDate.of(2026, 4, 1), 3,
                LocalDate.of(2026, 4, 25),
                89.90, PaymentType.CREDIT_CARD, "Parcela 1",
                new String[]{"1", "5678"});
        if (marcos != null) {
            addCreditPayment(marcos, 89.90, 10, 4, 2026, "Parcela 2 — abril");
        }

        // ---- Diego: matrícula cancelada em maio ----
        mockCancelled(fm, "77788899941", "Plano Semestral",
                LocalDate.of(2026, 5, 1), 6,
                LocalDate.of(2026, 5, 15),
                79.90, PaymentType.DEBIT_CARD, "Pagamento inicial",
                new String[]{"4321"});

        // ---- Fernanda: cancelar matrícula mensal atual, criar nova trimestral ----
        OperationResult<Enrollment> findFernanda = fm.findActiveEnrollmentByStudent("34650463238");
        if (findFernanda.isSuccess()) {
            fm.cancelEnrollment(findFernanda.getData().getCode());
        }
        fm.enrollStudent(
                "34650463238", "Plano Trimestral",
                formatDate(TODAY.plusDays(5)), 3,
                89.90, PaymentType.PIX, "Nova matrícula após cancelamento",
                new String[]{"fernanda.rocha@email.com"}
        );

        // ---- Mariana: cancelar matrícula anual atual, criar nova semestral ----
        OperationResult<Enrollment> findMariana = fm.findActiveEnrollmentByStudent("11122233396");
        if (findMariana.isSuccess()) {
            fm.cancelEnrollment(findMariana.getData().getCode());
        }
        OperationResult<Enrollment> c5 = fm.enrollStudent(
                "11122233396", "Plano Semestral",
                formatDate(TODAY.plusDays(6)), 6,
                79.90, PaymentType.CREDIT_CARD, "Nova matrícula — plano semestral",
                new String[]{"1", "5678"}
        );
        if (c5.isSuccess()) {
            addCreditPayment(c5.getData(), 79.90, 2, 6, 2026, "Parcela 2 — junho");
        }

        // ---- Letícia: cancelar mensal e re-matricular em trimestral ----
        OperationResult<Enrollment> findLeticia = fm.findActiveEnrollmentByStudent("23456789092");
        if (findLeticia.isSuccess()) {
            fm.cancelEnrollment(findLeticia.getData().getCode());
        }
        fm.enrollStudent(
                "23456789092", "Plano Trimestral",
                formatDate(TODAY.plusDays(7)), 3,
                89.90, PaymentType.DEBIT_CARD, "Nova matrícula trimestral",
                new String[]{"4321"}
        );
    }

    // ============================
    // Inativação de alunos
    // ============================

    private static void mockInactiveStudents(FitManager fm) {
        // Juliana, Marcos e Diego ficam inativos (matrículas já estão canceladas)
        fm.removeStudent("07859546434");
        fm.removeStudent("18345678904");
        fm.removeStudent("77788899941");
    }

    // ============================
    // Métodos de mock (helpers locais)
    // ============================

    /**
     * Constrói uma matrícula ACTIVE com a data de início informada (que pode
     * estar no passado) e adiciona ao sistema via {@link FitManager#mockEnrollment}.
     * Anexa um pagamento inicial com data igual à de início da matrícula.
     *
     * @return a matrícula criada, ou {@code null} se o plano não foi encontrado.
     */
    private static Enrollment mockActive(
            FitManager fm,
            String cpf,
            String planName,
            LocalDate startDate,
            int durationMonths,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {
        return mockEnrollmentInternal(fm, cpf, planName, startDate, durationMonths,
                EnrollmentStatus.ACTIVE, null,
                initialAmount, paymentType, paymentDescription, paymentData);
    }

    /**
     * Constrói uma matrícula CANCELLED com data de início e de cancelamento
     * arbitrárias e adiciona ao sistema via {@link FitManager#mockEnrollment}.
     * Anexa um pagamento inicial com data igual à de início da matrícula.
     */
    private static Enrollment mockCancelled(
            FitManager fm,
            String cpf,
            String planName,
            LocalDate startDate,
            int durationMonths,
            LocalDate cancelledAt,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {
        return mockEnrollmentInternal(fm, cpf, planName, startDate, durationMonths,
                EnrollmentStatus.CANCELLED, cancelledAt,
                initialAmount, paymentType, paymentDescription, paymentData);
    }

    private static Enrollment mockEnrollmentInternal(
            FitManager fm,
            String cpf,
            String planName,
            LocalDate startDate,
            int durationMonths,
            EnrollmentStatus status,
            LocalDate cancelledAt,
            double initialAmount,
            PaymentType paymentType,
            String paymentDescription,
            String[] paymentData
    ) {
        OperationResult<Plan> planResult = fm.findPlanByName(planName);
        if (!planResult.isSuccess()) {
            return null;
        }
        Plan plan = planResult.getData();

        int code = Enrollment.getNextCode();
        Enrollment.setNextCode(code + 1);

        double totalPrice = plan.calculateTotalPrice(durationMonths);
        Enrollment enrollment = new Enrollment(
                code, cpf, plan, startDate, durationMonths,
                totalPrice, status, cancelledAt
        );

        Payment initialPayment = PaymentFactory.create(
                paymentType, initialAmount,
                startDate.atTime(10, 0),
                paymentDescription, paymentData
        );
        enrollment.addPayment(initialPayment);

        fm.mockEnrollment(enrollment);
        return enrollment;
    }

    // ============================
    // Métodos Auxiliares
    // ============================

    /**
     * Formata um LocalDate para o padrão dd/MM/yyyy esperado pelo FitManager.
     */
    private static String formatDate(LocalDate date) {
        return String.format("%02d/%02d/%04d",
                date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    private static void addPixPayment(
            Enrollment enrollment, double amount,
            int day, int month, int year, String description
    ) {
        addPayment(enrollment, PaymentType.PIX, amount,
                dateTime(day, month, year), description,
                new String[]{"pix@email.com"});
    }

    private static void addCreditPayment(
            Enrollment enrollment, double amount,
            int day, int month, int year, String description
    ) {
        addPayment(enrollment, PaymentType.CREDIT_CARD, amount,
                dateTime(day, month, year), description,
                new String[]{"1", "5678"});
    }

    private static void addDebitPayment(
            Enrollment enrollment, double amount,
            int day, int month, int year, String description
    ) {
        addPayment(enrollment, PaymentType.DEBIT_CARD, amount,
                dateTime(day, month, year), description,
                new String[]{"4321"});
    }

    private static void addCashPayment(
            Enrollment enrollment, double amount,
            int day, int month, int year, String description
    ) {
        addPayment(enrollment, PaymentType.CASH, amount,
                dateTime(day, month, year), description,
                new String[]{String.valueOf(amount)});
    }

    /**
     * Anexa um pagamento com data arbitrária a uma matrícula. Usa a factory
     * de domínio para garantir a subclasse concreta correta; não passa pelo
     * service para preservar a data customizada.
     */
    private static void addPayment(
            Enrollment enrollment,
            PaymentType type,
            double amount,
            LocalDateTime paymentDate,
            String description,
            String[] paymentData
    ) {
        Payment payment = PaymentFactory.create(type, amount, paymentDate, description, paymentData);
        enrollment.addPayment(payment);
    }

    private static LocalDateTime dateTime(int day, int month, int year) {
        return LocalDateTime.of(year, month, day, 10, 0);
    }
}
