package ui.menus.plan;

import exceptions.FitManagerException;
import ui.screen.UserInterface;
import application.FitManager;
import application.OperationResult;
import domain.model.enums.PlanType;
import domain.model.plans.Plan;

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
            try {
                StringBuilder menuOptions = new StringBuilder();
                for (PlanMenuOption opt : PlanMenuOption.values()) {
                    menuOptions.append(opt.getNumber()).append(" - ").append(opt.getValorOpcao()).append("\n");
                }
                Integer choice = ui.showMenu("> GERENCIAR PLANOS", menuOptions.toString(), PlanMenuOption.values().length);

                if (choice == null) { running = false; continue; }

                PlanMenuOption option = PlanMenuOption.fromNumber(choice);
                if (option == null) continue;

                switch (option) {
                    case CADASTRAR:      registerPlan();   break;
                    case CONSULTAR_NOME: findPlanByName();    break;
                    case ALTERAR_PRECO:  updatePrice();   break;
                    case LISTAR:         listAllPlans();   break;
                    case VOLTAR:         running = false;   break;
                }
            } catch (FitManagerException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Fluxo de cadastro de novo plano.
     * Coleta dados via UserInterface e delega ao FitManager.
     */
    private void registerPlan() {
        String name = ui.getInput("Digite o nome do plano:", "Nome do plano");
        if (name == null) return;

        String description = ui.getInput("Digite a descrição do plano:", "Descrição do plano");
        if (description == null) return;

        PlanType type = selectPlanType();
        if (type == null) return;

        Integer minimumDurationBoxed = ui.getInt("Digite a duração mínima (em meses):", "Duração mínima");
        if (minimumDurationBoxed == null) return;
        int minimumDuration = minimumDurationBoxed;
        if (minimumDuration <= 0) {
            ui.showError("A duração mínima deve ser um número positivo.");
            return;
        }

        Double pricePerMonthBoxed = ui.getDouble("Digite o preço por mês (ex: 99,90):", "Preço por mês");
        if (pricePerMonthBoxed == null) return;
        double pricePerMonth = pricePerMonthBoxed;
        if (pricePerMonth <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult<Plan> result = fitManager.registerPlan(name, description, type, minimumDuration, pricePerMonth);

        if (result.isSuccess()) {
            Plan plan = result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta de plano pelo nome.
     */
    private void findPlanByName() {
        String name = ui.getInput("Digite o nome do plano para consulta:", "Nome do plano");
        if (name == null) return;

        OperationResult<Plan> result = fitManager.findPlanByName(name);

        if (result.isSuccess()) {
            Plan plan = result.getData();
            ui.showMessage("Plano encontrado:\n\n" + plan.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de atualização do preço de um plano.
     */
    private void updatePrice() {
        String name = ui.getInput("Digite o nome do plano a atualizar:", "Nome do plano");
        if (name == null) return;

        OperationResult<Plan> findResult = fitManager.findPlanByName(name);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Plan plan = findResult.getData();
        ui.showMessage("Plano encontrado:\n\n" + plan.toString());

        Double newPriceBoxed = ui.getDouble("Digite o novo preço por mês (ex: 99,90):", "Novo preço");
        if (newPriceBoxed == null) return;
        double newPrice = newPriceBoxed;
        if (newPrice <= 0) {
            ui.showError("O preço deve ser um valor positivo.");
            return;
        }

        OperationResult<Plan> result = fitManager.updatePlanPrice(name, newPrice);

        if (result.isSuccess()) {
            Plan updated = result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados atualizados:\n" + updated.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os planos cadastrados.
     */
    private void listAllPlans() {
        OperationResult<ArrayList<Plan>> result = fitManager.listAllPlans();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Plan> plans = result.getData();
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
        StringBuilder options = new StringBuilder("Escolha o tipo de plano:\n");
        int count = 1;
        for (PlanType type : PlanType.values()) {
            options.append(count).append(" - ").append(type.getLabel()).append("\n");
            count++;
        }

        Integer choice = ui.showMenu("> TIPO DE PLANO", options.toString(), PlanType.values().length);
        if (choice == null) return null;
        return PlanType.values()[choice - 1];
    }
}
