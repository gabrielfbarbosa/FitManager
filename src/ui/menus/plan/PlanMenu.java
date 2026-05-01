package ui.menus.plan;

import ui.screen.UserInterface;
import application.FitManager;
import application.OperationResult;
import domain.model.enums.PlanType;
import domain.model.plans.Plan;
import ui.screen.InputParser;

import java.util.ArrayList;

/**
 * Menu de gerenciamento de planos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Mantém referência à UserInterface (para interação) e ao FitManager
 * (para execução das operações).
 */
public class PlanMenu {

    private final UserInterface ui;
    private final FitManager fitManager;

    public PlanMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de planos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            String menuOptions = "";
            for (PlanMenuOption opt : PlanMenuOption.values()) {
                menuOptions += opt.getNumber() + " - " + opt.getValorOpcao() + "\n";
            }
            String input = ui.showMenu("> GERENCIAR PLANOS", menuOptions);

            if (input == null) { running = false; continue; }
            if (!InputParser.isNumeric(input)) {
                ui.showError("Opção inválida. Digite um número de 1 a " + PlanMenuOption.values().length + ".");
                continue;
            }

            PlanMenuOption option = PlanMenuOption.fromNumber(Integer.parseInt(input.trim()));

            if (option == null) {
                ui.showError("Opção inválida. Escolha de 1 a " + PlanMenuOption.values().length + ".");
                continue;
            }

            switch (option) {
                case CADASTRAR:      registerPlan();   break;
                case CONSULTAR_NOME: findPlanByName();    break;
                case ALTERAR_PRECO:  updatePrice();   break;
                case LISTAR:         listAllPlans();   break;
                case VOLTAR:         running = false;   break;
            }
        }
    }

    /**
     * Fluxo de cadastro de novo plano.
     * Coleta dados via UserInterface e delega ao FitManager.
     */
    private void registerPlan() {
        String name = ui.getInput("Digite o nome do plano:");
        if (name == null) return;

        String description = ui.getInput("Digite a descrição do plano:");
        if (description == null) return;

        PlanType type = selectPlanType();
        if (type == null) return;

        String minDurationStr = ui.getInput("Digite a duração mínima (em meses):");
        if (minDurationStr == null) return;

        int minimumDuration = InputParser.parseIntSafe(minDurationStr);
        if (minimumDuration == InputParser.INVALID_INT || minimumDuration <= 0) {
            ui.showError("A duração mínima deve ser um número positivo.");
            return;
        }

        String priceStr = ui.getInput("Digite o preço por mês (ex: 99,90):");
        if (priceStr == null) return;

        double pricePerMonth = InputParser.parseDoubleSafe(priceStr);
        if (pricePerMonth == InputParser.INVALID_DOUBLE || pricePerMonth <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult result = fitManager.registerPlan(name, description, type, minimumDuration, pricePerMonth);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta de plano pelo nome.
     */
    private void findPlanByName() {
        String name = ui.getInput("Digite o nome do plano para consulta:");
        if (name == null) return;

        OperationResult result = fitManager.findPlanByName(name);

        if (result.isSuccess()) {
            Plan plan = (Plan) result.getData();
            ui.showMessage("Plano encontrado:\n\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de atualização do preço de um plano.
     */
    private void updatePrice() {
        String name = ui.getInput("Digite o nome do plano a atualizar:");
        if (name == null) return;

        OperationResult findResult = fitManager.findPlanByName(name);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Plan plan = (Plan) findResult.getData();
        ui.showMessage("Plano encontrado:\n\n" + plan.toString());

        String newPriceStr = ui.getInput("Digite o novo preço por mês (ex: 99,90):");
        if (newPriceStr == null) return;

        double newPrice = InputParser.parseDoubleSafe(newPriceStr);
        if (newPrice == InputParser.INVALID_DOUBLE || newPrice <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult result = fitManager.updatePlanPrice(name, newPrice);

        if (result.isSuccess()) {
            Plan updated = (Plan) result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados atualizados:\n" + updated.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os planos cadastrados.
     */
    private void listAllPlans() {
        OperationResult result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Plan> plans = (ArrayList<Plan>) result.getData();
        String message = "> PLANOS CADASTRADOS\n";
        message += "Total: " + plans.size() + " plano(s)\n\n";

        for (int i = 0; i < plans.size(); i++) {
            message += "--- Plano " + (i + 1) + " ---\n";
            message += plans.get(i).toString();
            if (i < plans.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }

    /**
     * Auxilia na seleção de um tipo de plano.
     * Exibe todas as opções e retorna a escolha do usuário.
     */
    private PlanType selectPlanType() {
        String options = "Escolha o tipo de plano:\n";
        int count = 1;
        for (PlanType type : PlanType.values()) {
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
            if (choice >= 1 && choice <= PlanType.values().length) {
                return PlanType.values()[choice - 1];
            } else {
                ui.showError("Opção inválida. Escolha de 1 a " + PlanType.values().length + ".");
            }
        }
    }
}