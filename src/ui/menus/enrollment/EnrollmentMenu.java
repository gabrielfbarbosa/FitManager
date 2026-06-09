package ui.menus.enrollment;

import application.FitManager;
import application.OperationResult;
import domain.model.enums.PaymentType;
import domain.model.Enrollment;
import domain.model.payments.Payment;
import exceptions.FitManagerException;
import ui.screen.UserInterface;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;
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
            try {
                String menuOptions = "";
                for (EnrollmentMenuOption opt : EnrollmentMenuOption.values()) {
                    menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
                }
                Integer choice = ui.showMenu("> GERENCIAR MATRÍCULAS", menuOptions, EnrollmentMenuOption.values().length);

                if (choice == null) { running = false; continue; }

                EnrollmentMenuOption option = EnrollmentMenuOption.fromNumber(choice);
                if (option == null) continue;

                switch (option) {
                    case MATRICULAR:        enrollStudent();          break;
                    case CONSULTAR_ATIVA:   findActiveEnrollment();   break;
                    case HISTORICO:         listHistory();            break;
                    case CANCELAR:          cancelEnrollment();       break;
                    case REGISTRAR_PAGAMENTO: registerPayment();     break;
                    case VOLTAR:            running = false;          break;
                }
            } catch (FitManagerException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Fluxo de matrícula de um aluno em um plano.
     */
    private void enrollStudent() {
        String cpf = askValidCpf("Digite o CPF do aluno:");
        if (cpf == null) return;

        String planName = ui.getInput("Digite o nome do plano:", "Nome do plano");
        if (planName == null) return;

        LocalDate startDate = ui.getDate("Digite a data de início da matrícula (dd/mm/aaaa):", "Data de início");
        if (startDate == null) return;
        String startDateStr = DateFormatter.formatDate(startDate);

        Integer durationBoxed = ui.getInt("Digite a duração (em meses):", "Duração do plano");
        if (durationBoxed == null) return;
        int durationMonths = durationBoxed;
        if (durationMonths <= 0) {
            ui.showError("A duração deve ser um número positivo.");
            return;
        }

        Double initialAmountBoxed = ui.getDouble("Digite o valor do pagamento inicial (ex: 99,90):", "Valor do pagamento inicial");
        if (initialAmountBoxed == null) return;
        double initialAmount = initialAmountBoxed;
        if (initialAmount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String[] paymentData = collectPaymentData(paymentType, initialAmount);
        if (paymentData == null) return;

        String paymentDescription = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (paymentDescription == null) paymentDescription = "Pagamento inicial de matrícula";

        OperationResult<Enrollment> result = fitManager.enrollStudent(cpf, planName, startDateStr,
                durationMonths, initialAmount, paymentType, paymentDescription, paymentData);

        if (result.isSuccess()) {
            Enrollment enrollment = result.getData();
            ui.showMessage(result.getMessage() + "\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta da matrícula ativa de um aluno.
     */
    private void findActiveEnrollment() {
        String cpf = askValidCpf("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult<Enrollment> result = fitManager.findActiveEnrollmentByStudent(cpf);

        if (result.isSuccess()) {
            Enrollment enrollment = result.getData();
            ui.showMessage("Matrícula ativa encontrada:\n\n" + buildEnrollmentSummary(enrollment));
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem do histórico de matrículas de um aluno.
     */
    private void listHistory() {
        String cpf = askValidCpf("Digite o CPF do aluno:");
        if (cpf == null) return;

        OperationResult<ArrayList<Enrollment>> result = fitManager.listEnrollmentHistory(cpf);

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Enrollment> enrollments = result.getData();
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
        Integer codeBoxed = ui.getInt("Digite o código da matrícula a cancelar:", "Código da matrícula");
        if (codeBoxed == null) return;
        int code = codeBoxed;

        String confirm = ui.getInput("Tem certeza que deseja cancelar a matrícula " + code +
                "?\nDigite 'S' para confirmar ou qualquer outra tecla para cancelar:");
        if (confirm == null || !confirm.trim().equalsIgnoreCase("S")) {
            ui.showMessage("Operação cancelada.");
            return;
        }

        OperationResult<Enrollment> result = fitManager.cancelEnrollment(code);

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
        Integer codeBoxed = ui.getInt("Digite o código da matrícula:", "Código da matrícula");
        if (codeBoxed == null) return;
        int code = codeBoxed;

        Double amountBoxed = ui.getDouble("Digite o valor do pagamento (ex: 99,90):", "Valor do pagamento");
        if (amountBoxed == null) return;
        double amount = amountBoxed;
        if (amount <= 0) {
            ui.showError("O valor deve ser positivo.");
            return;
        }

        PaymentType paymentType = selectPaymentType();
        if (paymentType == null) return;

        String[] paymentData = collectPaymentData(paymentType, amount);
        if (paymentData == null) return;

        String description = ui.getInput("Digite uma descrição para o pagamento (opcional):");
        if (description == null) description = "Pagamento adicional";

        OperationResult<Payment> result = fitManager.registerPayment(code, amount, paymentType, description, paymentData);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Apresenta o menu de tipos de pagamento e devolve o {@link PaymentType}
     * escolhido pelo usuário. Reutiliza {@link #{ui.showMenu}(String, String, int)}
     * para já validar opção numéricano intervalo.
     * Cancel/fechar retorna {@code null}.
     */
    private PaymentType selectPaymentType() {
        StringBuilder options = new StringBuilder("Escolha o tipo de pagamento:\n");
        int count = 1;
        for (PaymentType type : PaymentType.values()) {
            options.append(count).append(" - ").append(type.getLabel()).append("\n");
            count++;
        }
        Integer choice = ui.showMenu("> TIPO DE PAGAMENTO", options.toString(), PaymentType.values().length);
        if (choice == null) return null;
        return PaymentType.values()[choice - 1];
    }

    /**
     * Coleta os dados específicos de cada tipo de pagamento com loops de
     * validação locais — o usuário não avança para o próximo campo
     * enquanto o dado atual não for válido. Implementação default
     * compartilhada entre as duas UIs.
     *
     *  - {@code PIX}: chave PIX obrigatória.
     *  - {@code CREDIT_CARD}: número de parcelas positivo + últimos 4
     *    dígitos numéricos do cartão.
     *  - {@code DEBIT_CARD}: últimos 4 dígitos numéricos do cartão.
     *  - {@code CASH}: valor recebido positivo e {@code >= amount}.
     *
     * Cancel/fechar em qualquer campo retorna {@code null}, abortando o
     * fluxo de pagamento sem deixar o estado pela metade.
     */
    private String[] collectPaymentData(PaymentType paymentType, double amount) {
        switch (paymentType) {
            case PIX:
                String pixKey = ui.getInput("Digite a chave PIX:", "Chave PIX");
                if (pixKey == null) return null;
                return new String[]{pixKey};

            case CREDIT_CARD:
                int installments;
                while (true) {
                    Integer i = ui.getInt("Digite o número de parcelas:", "Número de parcelas");
                    if (i == null) return null;
                    if (i > 0) { installments = i; break; }
                    ui.showError("O número de parcelas deve ser um inteiro positivo.");
                }
                String creditDigits;
                while (true) {
                    String d = ui.getInput("Digite os últimos 4 dígitos do cartão:", "Últimos 4 dígitos do cartão");
                    if (d == null) return null;
                    if (isFourDigitNumber(d)) { creditDigits = d; break; }
                    ui.showError("Informe exatamente 4 dígitos numéricos.");
                }
                return new String[]{String.valueOf(installments), creditDigits};

            case DEBIT_CARD:
                String debitDigits;
                while (true) {
                    String d = ui.getInput("Digite os últimos 4 dígitos do cartão:", "Últimos 4 dígitos do cartão");
                    if (d == null) return null;
                    if (isFourDigitNumber(d)) { debitDigits = d; break; }
                    ui.showError("Informe exatamente 4 dígitos numéricos.");
                }
                return new String[]{debitDigits};

            case CASH:
                double received;
                while (true) {
                    Double r = ui.getDouble(
                            "Digite o valor recebido em dinheiro (Valor a receber " +
                                    CurrencyFormatter.formatCurrency(amount) + "):",
                            "Valor recebido em dinheiro"
                    );
                    if (r == null) return null;
                    if (r <= 0) { ui.showError("O valor recebido deve ser positivo."); continue; }
                    if (r < amount) {
                        ui.showError("O valor recebido (" + CurrencyFormatter.formatCurrency(r) +
                                ") deve ser maior ou igual ao valor do pagamento (" +
                                CurrencyFormatter.formatCurrency(amount) + ").");
                        continue;
                    }
                    received = r;
                    break;
                }
                return new String[]{String.valueOf(received)};

            default:
                return new String[0];
        }
    }

    /**
     * Verifica se {@code value} é composta por exatamente 4 dígitos numéricos.
     * Usado por {@link #collectPaymentData(PaymentType, double)} para validar
     * os últimos 4 dígitos do cartão.
     */
    private static boolean isFourDigitNumber(String value) {
        if (value == null || value.length() != 4) return false;
        return value.chars().allMatch(Character::isDigit);
    }

    /**
     * Verificar se um cpf é valido, e solicitar novamente
     * caso não for valido
    */
    private String askValidCpf(String prompt) {

        while (true) {

            String cpf = ui.getInput(prompt, "CPF");

            OperationResult<String> result = fitManager.validateCpf(cpf);

            if (result.isSuccess()) {
                return result.getData();
            }

            ui.showError(result.getMessage());
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
