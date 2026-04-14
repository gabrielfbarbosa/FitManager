package mocks;

import application.FitManager;
import application.OperationResult;
import domain.model.Enrollment;
import domain.model.enums.PaymentType;
import domain.model.enums.PlanType;

/**
 * Popula o sistema com dados de teste para facilitar os testes manuais.
 *
 * Cenários cobertos:
 * - alunos ativos e inativos
 * - alunos sem matrícula
 * - matrículas ativas
 * - matrículas canceladas
 * - histórico com mais de uma matrícula para o mesmo aluno
 * - aluno inativo com histórico de matrícula
 */
public class DataMock {

    public static void populateDemo(FitManager fm) {
        mockPlans(fm);
        mockStudents(fm);
        mockEnrollments(fm);
        mockInactiveStudents(fm);
    }

    // ============================
    // Planos
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
    // Alunos
    // ============================

    private static void mockStudents(FitManager fm) {
        // Ativo com matrícula ativa e pagamentos parciais
        fm.registerStudent(
                "Carlos Eduardo Silva",
                "52998224725",
                "carlos.silva@email.com",
                "15/03/1995"
        );

        // Ativa com matrícula ativa e saldo pendente
        fm.registerStudent(
                "Ana Paula Ferreira",
                "71428793860",
                "(67) 99123-4567",
                "22/07/1998"
        );

        // Ativo com matrícula ativa quitada
        fm.registerStudent(
                "Bruno Henrique Costa",
                "87748248800",
                "bruno.costa@gmail.com",
                "08/11/1990"
        );

        // Ativa com matrícula cancelada
        fm.registerStudent(
                "Fernanda Lima Rocha",
                "34650463238", //"34650463280",
                "fernanda.rocha@email.com",
                "30/01/2000"
        );

        // Ativo sem matrícula
        fm.registerStudent(
                "Ricardo Mendes Alves",
                "47093450822",//"47093450819",
                "(67) 98765-4321",
                "14/06/1985"
        );

        // Ficará inativa, mas com histórico de matrícula cancelada
        fm.registerStudent(
                "Juliana Torres Souza",
                "07859546434", //"07859546431",
                "ju.torres@email.com",
                "03/09/1993"
        );

        // Ficará inativo e sem matrícula
        fm.registerStudent(
                "Marcos Vinicius Lima",
                "18345678904", //"18345678909",
                "marcos.lima@email.com",
                "11/12/1992"
        );

        // Ativa com matrícula ativa e pagamentos parciais (3 de 6 meses pagos)
        fm.registerStudent(
                "Patrícia Oliveira Santos",
                "12345678909",
                "patricia.santos@email.com",
                "25/05/1997"
        );

        // Ativo com matrícula ativa totalmente quitada
        fm.registerStudent(
                "Lucas Rodrigues Ferreira",
                "98765432100",
                "(67) 99234-5678",
                "10/02/1994"
        );

        // Ativa com histórico: matrícula cancelada + nova matrícula ativa
        fm.registerStudent(
                "Mariana Beatriz Campos",
                "11122233396",
                "mariana.campos@gmail.com",
                "17/09/2001"
        );

        // Ativo sem matrícula (apenas cadastrado)
        fm.registerStudent(
                "Thiago Nascimento Gomes",
                "44455566619",
                "(67) 98321-4567",
                "03/07/1988"
        );

        // Ativa com matrícula ativa e saldo pendente (entrada simbólica)
        fm.registerStudent(
                "Letícia Aparecida Moura",
                "23456789092",
                "leticia.moura@email.com",
                "12/11/1999"
        );

        // Ficará inativo sem matrícula
        fm.registerStudent(
                "Diego Henrique Prado",
                "77788899941",
                "diego.prado@email.com",
                "29/04/1991"
        );
    }

    // ============================
    // Matrículas e Pagamentos
    // ============================

    private static void mockEnrollments(FitManager fm) {

        // Matrícula 1 — Carlos / ativa / parcial (3 de 12 parcelas pagas)
        OperationResult r1 = fm.enrollStudent(
                "52998224725",
                "Plano Anual",
                "13/08/2026",
                12,
                69.90,
                PaymentType.PIX,
                "1ª parcela — abril"
        );
        if (r1.isSuccess()) {
            int code = ((Enrollment) r1.getData()).getCode();
            fm.registerPayment(code, 69.90, PaymentType.PIX,        "2ª parcela — maio");
            fm.registerPayment(code, 69.90, PaymentType.DEBIT_CARD, "3ª parcela — junho");
        }

        // Matrícula 2 — Ana / ativa / saldo pendente
        fm.enrollStudent(
                "71428793860",
                "Plano Mensal",
                "13/09/2026",
                1,
                50.00,
                PaymentType.CASH,
                "Entrada parcial"
        );

        // Matrícula 3 — Bruno / ativa / quitada
        OperationResult r3 = fm.enrollStudent(
                "87748248800",
                "Plano Trimestral",
                "13/10/2026",
                3,
                89.90,
                PaymentType.CREDIT_CARD,
                "1ª parcela"
        );
        if (r3.isSuccess()) {
            int code = ((Enrollment) r3.getData()).getCode();
            fm.registerPayment(code, 89.90, PaymentType.CREDIT_CARD, "2ª parcela");
            fm.registerPayment(code, 89.90, PaymentType.CREDIT_CARD, "3ª parcela — quitado");
        }

        // Matrícula 4 — Fernanda / cancelada
        OperationResult r4 = fm.enrollStudent(
                "34650463238",
                "Plano Semestral",
                "13/11/2026",
                6,
                79.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );
        if (r4.isSuccess()) {
            fm.cancelEnrollment(((Enrollment) r4.getData()).getCode());
        }

        // Matrícula 5 — Juliana / cancelada, depois a aluna será inativada
        OperationResult r5 = fm.enrollStudent(
                "07859546434",
                "Plano Mensal",
                "13/12/2026",
                1,
                99.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );
        if (r5.isSuccess()) {
            fm.cancelEnrollment(((Enrollment) r5.getData()).getCode());
        }

        // Matrícula 6 — Fernanda novamente, agora ativa
        // Serve para testar histórico com múltiplas matrículas no mesmo CPF
        fm.enrollStudent(
                "34650463238",
                "Plano Trimestral",
                "13/11/2026",
                3,
                89.90,
                PaymentType.CREDIT_CARD,
                "Nova matrícula após cancelamento anterior"
        );

        // Patrícia / Plano Semestral / ativa / 3 de 6 parcelas pagas
        OperationResult r7 = fm.enrollStudent(
                "12345678909",
                "Plano Semestral",
                "15/04/2026",
                6,
                79.90,
                PaymentType.PIX,
                "1ª parcela — abril"
        );
        if (r7.isSuccess()) {
            int code = ((Enrollment) r7.getData()).getCode();
            fm.registerPayment(code, 79.90, PaymentType.PIX,         "2ª parcela — maio");
            fm.registerPayment(code, 79.90, PaymentType.DEBIT_CARD,  "3ª parcela — junho");
        }

        // Lucas / Plano Mensal / ativa / totalmente quitada
        OperationResult r8 = fm.enrollStudent(
                "98765432100",
                "Plano Mensal",
                "15/04/2026",
                1,
                99.90,
                PaymentType.CREDIT_CARD,
                "Pagamento integral"
        );

        // Mariana / 1ª matrícula (Plano Trimestral) → cancelada
        OperationResult r9 = fm.enrollStudent(
                "11122233396",
                "Plano Trimestral",
                "15/04/2026",
                3,
                89.90,
                PaymentType.CASH,
                "1ª parcela"
        );
        if (r9.isSuccess()) {
            int code = ((Enrollment) r9.getData()).getCode();
            fm.cancelEnrollment(code);
        }

        // Mariana / 2ª matrícula (Plano Anual) → ativa / parcialmente paga
        OperationResult r10 = fm.enrollStudent(
                "11122233396",
                "Plano Anual",
                "15/04/2026",
                12,
                69.90,
                PaymentType.PIX,
                "1ª parcela — nova matrícula após cancelamento"
        );
        if (r10.isSuccess()) {
            int code = ((Enrollment) r10.getData()).getCode();
            fm.registerPayment(code, 69.90, PaymentType.PIX, "2ª parcela");
        }

        // Letícia / Plano Trimestral / ativa / entrada simbólica (saldo pendente alto)
        fm.enrollStudent(
                "23456789092",
                "Plano Trimestral",
                "15/04/2026",
                3,
                50.00,
                PaymentType.CASH,
                "Entrada parcial"
        );
    }

    // ============================
    // Inativação de alunos
    // ============================

    private static void mockInactiveStudents(FitManager fm) {
        // Juliana fica inativa, mas mantém histórico
        fm.removeStudent("07859546434");

        // Marcos fica inativo e sem matrícula
        fm.removeStudent("18345678904");

        // Diego fica inativo sem matrícula
        fm.removeStudent("77788899941");
    }
}