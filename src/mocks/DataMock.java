package mocks;

import application.FitManager;
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

    public static void mock(FitManager fm) {
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
    }

    // ============================
    // Matrículas e Pagamentos
    // ============================

    private static void mockEnrollments(FitManager fm) {

        // Matrícula 1 — Carlos / ativa / parcial
        fm.enrollStudent(
                "52998224725",
                "Plano Anual",
                "01/01/2026",
                12,
                69.90,
                PaymentType.PIX,
                "1ª parcela — janeiro"
        );
        fm.registerPayment(1, 69.90, PaymentType.PIX, "2ª parcela — fevereiro");
        fm.registerPayment(1, 69.90, PaymentType.DEBIT_CARD, "3ª parcela — março");

        // Matrícula 2 — Ana / ativa / saldo pendente
        fm.enrollStudent(
                "71428793860",
                "Plano Mensal",
                "01/03/2026",
                1,
                50.00,
                PaymentType.CASH,
                "Entrada parcial"
        );

        // Matrícula 3 — Bruno / ativa / quitada
        fm.enrollStudent(
                "87748248800",
                "Plano Trimestral",
                "01/01/2026",
                3,
                89.90,
                PaymentType.CREDIT_CARD,
                "1ª parcela"
        );
        fm.registerPayment(3, 89.90, PaymentType.CREDIT_CARD, "2ª parcela");
        fm.registerPayment(3, 89.90, PaymentType.CREDIT_CARD, "3ª parcela — quitado");

        // Matrícula 4 — Fernanda / cancelada
        fm.enrollStudent(
                "34650463238", //34650463280
                "Plano Semestral",
                "01/02/2026",
                6,
                79.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );
        fm.cancelEnrollment(4);

        // Matrícula 5 — Juliana / cancelada, depois a aluna será inativada
        fm.enrollStudent(
                "07859546434", // "07859546431",
                "Plano Mensal",
                "10/02/2026",
                1,
                99.90,
                PaymentType.PIX,
                "Pagamento inicial"
        );
        fm.cancelEnrollment(5);

        // Matrícula 6 — Fernanda novamente, agora ativa
        // Serve para testar histórico com múltiplas matrículas no mesmo CPF
        fm.enrollStudent(
                "34650463238",
                "Plano Trimestral",
                "15/03/2026",
                3,
                89.90,
                PaymentType.CREDIT_CARD,
                "Nova matrícula após cancelamento anterior"
        );
    }

    // ============================
    // Inativação de alunos
    // ============================

    private static void mockInactiveStudents(FitManager fm) {
        // Juliana fica inativa, mas mantém histórico
        fm.removeStudent("07859546434");//"07859546431"

        // Marcos fica inativo e sem matrícula
        fm.removeStudent("18345678904"); // "18345678909"
    }
}