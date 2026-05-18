package ui.menus.enrollment;

import application.FitManager;
import application.OperationResult;
import domain.model.enums.PaymentType;
import domain.model.Enrollment;
import ui.screen.InputParser;
import ui.screen.UserInterface;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.util.ArrayList;

/**
 * Menu de gerenciamento de matrículas.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class EnrollmentMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public EnrollmentMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de matrículas.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (EnrollmentMenuOption opt : EnrollmentMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR MATRÍCULAS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            EnrollmentMenuOption option = EnrollmentMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + EnrollmentMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case MATRICULAR:        enrollStudent();          break;
                case CONSULTAR_ATIVA:   findActiveEnrollment();   break;
                case HISTORICO:         listHistory();            break;
                case CANCELAR:          cancelEnrollment();       break;
                case REGISTRAR_PAGAMENTO: registerPayment();     break;
                case VOLTAR:            running = false;          break;
            }
        }
    }

    /**
     * Fluxo de matrícula de um aluno em um plano.
     */
    private void enrollStudent() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        String planName = ui.getInput("Digite o nome do plano:");
        if (planName == null) return;

        String startDateStr = ui.getInput("Digite a data de início da matrícula (dd/mm/aaaa):");
        if (startDateStr == null) return;

        String durationStr = ui.getInput("Digite a duração (em meses):");
        if (durationStr == null) return;

        int durationMonths = InputParser.parseIntSafe(durationStr);
        if (durationMonths == InputParser.INVALID_INT || durationMonths <= 0) {
            ui.showError("A duração deve ser um número positivo.");
            return;
        }

        String initialAmountStr = ui.getInput("Digite o valor do pagamento inicial (ex: 99,90):");
        if (initialAmountStr == null) return;

        double initialAmount = InputParser.parseDoubleSafe(initialAmountStr);
        if (initialAmount == InputParser.INVALID_DOUBLE || initialAmount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String paymentDescription = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (paymentDescription == null) paymentDescription = "Pagamento inicial de matrícula";

        String[] paymentData = collectPaymentData(paymentType, initialAmount);
        if (paymentData == null) return;

        OperationResult result = fitManager.enrollStudent(cpf, planName, startDateStr,
                durationMonths, initialAmount, paymentType, paymentDescription, paymentData);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage(result.getMessage() + "\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta da matrícula ativa de um aluno.
     */
    private void findActiveEnrollment() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = (Enrollment) result.getData();
            ui.showMessage("Matrícula ativa encontrada:\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem do histórico de matrículas de um aluno.
     */
    private void listHistory() {
        String cpf = ui.getInput("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult result = fitManager.listEnrollmentHistory(cpf);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = (ArrayList<Enrollment>) result.getData();
        String message = "> HISTÓRICO DE MATRÍCULAS\n";
        message += "Total: " + enrollments.size() + " matrícula(s)\n\n";

        for (int i = 0; i < enrollments.size(); i++) {
            message += "--- Matrícula " + (i + 1) + " ---\n";
            message += enrollments.get(i).toString();
            if (i < enrollments.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Fluxo de cancelamento de uma matrícula.
     */
    private void cancelEnrollment() {
        String codeStr = ui.getInput("Digite o código da matrícula a cancelar:");
        if (codeStr == null) return;

        int code = InputParser.parseIntSafe(codeStr);
        if (code == InputParser.INVALID_INT) {
            ui.showError("Código inválido.");
            return;
        }

        String confirm = ui.getInput("Tem certeza que deseja cancelar a matrícula " + code +
                "?\nDigite 'S' para confirmar ou qualquer outra tecla para cancelar:");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("S")) {
            ui.showMessage("Operação cancelada.");
            return;
        }

        OperationResult result = fitManager.cancelEnrollment(code);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de registro de um novo pagamento para uma matrícula.
     */
    private void registerPayment() {
        String codeStr = ui.getInput("Digite o código da matrícula:");
        if (codeStr == null) return;

        int code = InputParser.parseIntSafe(codeStr);
        if (code == InputParser.INVALID_INT) {
            ui.showError("Código inválido.");
            return;
        }

        String amountStr = ui.getInput("Digite o valor do pagamento (ex: 99,90):");
        if (amountStr == null) return;

        double amount = InputParser.parseDoubleSafe(amountStr);
        if (amount == InputParser.INVALID_DOUBLE || amount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String description = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (description == null) description = "Pagamento adicional";

        String[] paymentData = collectPaymentData(paymentType, amount);
        if (paymentData == null) return;

        OperationResult result = fitManager.registerPayment(code, amount, paymentType, description, paymentData);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Auxilia na seleção de um tipo de pagamento.
     * Exibe todas as opções e retorna a escolha do usuário.
     */
    private PaymentType selectPaymentType() {
        String options = "Escolha o tipo de pagamento:\n";
        int count = 1;
        for (PaymentType type : PaymentType.values()) {
            options += count + " - " + type.getLabel() + "\n";
            count++;
        }

        while (true) {
            String input = ui.getInput(options + "\nOpção:");
            if (input == null) return null;

            if (!InputParser.isNumeric(input)) {
                ui.showError("Digite um número válido.");
                continue;
            }

            int choice = Integer.parseInt(input.trim());
            if (choice >= 1 && choice <= PaymentType.values().length) {
                return PaymentType.values()[choice - 1];
            } else {
                ui.showError("Opção inválida. Escolha de 1 a " + PaymentType.values().length + ".");
            }
        }
    }

    /**
     * Coleta dados adicionais específicos do tipo de pagamento.
     */
    private String[] collectPaymentData(PaymentType paymentType, double amount) {
        switch (paymentType) {
            case PIX:
                String pixKey = ui.getInput("Digite a chave PIX:");
                if (pixKey == null) return null;
                return new String[]{pixKey};

            case CREDIT_CARD:
                String installmentsStr = ui.getInput("Digite a quantidade de parcelas:");
                if (installmentsStr == null) return null;
                int installments = InputParser.parseIntSafe(installmentsStr);
                if (installments == InputParser.INVALID_INT || installments <= 0) {
                    ui.showError("Número de parcelas inválido.");
                    return null;
                }

                String creditDigits = ui.getInput("Digite os 4 últimos dígitos do cartão:");
                if (creditDigits == null) return null;
                return new String[]{String.valueOf(installments), creditDigits};

            case DEBIT_CARD:
                String debitDigits = ui.getInput("Digite os 4 últimos dígitos do cartão:");
                if (debitDigits == null) return null;
                return new String[]{debitDigits};

            case CASH:
                String receivedStr = ui.getInput(
                        "Digite o valor recebido em dinheiro (>= " + CurrencyFormatter.formatCurrency(amount) + "):"
                );
                if (receivedStr == null) return null;
                return new String[]{receivedStr};

            default:
                return new String[0];
        }
    }

    /**
     * Constrói um sumário formatado de uma matrícula para exibição.
     */
    private String buildEnrollmentSummary(Enrollment enrollment) {
        return "Código: " + enrollment.getCode() + "\n" +
                "Plano: " + enrollment.getPlanName() + "\n" +
                "Data Início: " + DateFormatter.formatDate(enrollment.getStartDate()) + "\n" +
                "Data Fim: " + DateFormatter.formatDate(enrollment.getEndDate()) + "\n" +
                "Duração: " + enrollment.getDurationMonths() +
                (enrollment.getDurationMonths() == 1 ? " mês" : " meses") + "\n" +
                "Preço Total: " + CurrencyFormatter.formatCurrency(enrollment.getTotalPrice()) + "\n" +
                "Saldo Pendente: " + CurrencyFormatter.formatCurrency(enrollment.calculateBalance()) + "\n" +
                "Status: " + enrollment.getStatus().getLabel() + "\n" +
                "Pagamentos Registrados: " + enrollment.getPayments().size();
    }
}