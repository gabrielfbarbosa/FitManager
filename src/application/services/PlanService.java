package application.services;

import application.OperationResult;
import domain.model.enums.PlanType;
import domain.model.plans.Plan;
import domain.model.plans.MonthlyPlan;
import domain.model.plans.QuarterlyPlan;
import domain.model.plans.SemiAnnualPlan;
import domain.model.plans.AnnualPlan;
import exceptions.DuplicatedPlanException;
import exceptions.RequiredFieldException;
import util.CurrencyFormatter;

import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de planos em memória
 * e implementar as operações específicas da entidade Plan.
 *
 * Política de comunicação de falhas:
 * - Campos obrigatórios vazios → {@link RequiredFieldException}.
 * - Nome de plano duplicado → {@link DuplicatedPlanException}.
 * - Demais validações (valores não positivos, plano não encontrado) seguem
 *   usando {@link OperationResult} com {@code success = false}.
 */
public class PlanService {

    private ArrayList<Plan> plans;

    public PlanService() {
        this.plans = new ArrayList<>();
    }

    /**
     * Registra um novo plano no sistema.
     */
    public OperationResult registerPlan(
            String name,
            String description,
            PlanType type,
            int minimumDuration,
            double pricePerMonth
    ) {
        if (name == null || name.trim().isEmpty()) {
            throw new RequiredFieldException("nome do plano");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new RequiredFieldException("descrição do plano");
        }
        if (type == null) {
            throw new RequiredFieldException("tipo do plano");
        }

        if (minimumDuration <= 0) {
            return new OperationResult(false, "A duração mínima deve ser maior que zero.");
        }
        if (pricePerMonth <= 0) {
            return new OperationResult(false, "O preço por mês deve ser um valor positivo.");
        }

        if (nameExists(name.trim())) {
            throw new DuplicatedPlanException(name.trim());
        }

        // Instancia a subclasse correta com base no tipo informado.
        Plan plan = createPlanByType(
            name.trim(),
            description.trim(),
            type,
            minimumDuration,
            pricePerMonth
        );

        plans.add(plan);

        return new OperationResult(true,
                "✅ Plano \"" + plan.getName() + "\" registrado com sucesso!", plan);
    }

    /**
     * Instancia a subclasse correta de Plan com base no PlanType.
     */
    private Plan createPlanByType(
        String name,
        String description,
        PlanType type,
        int minimumDuration,
        double pricePerMonth
    ) {
        return switch (type) {
            case MONTHLY -> new MonthlyPlan(name, description, minimumDuration, pricePerMonth);
            case QUARTERLY -> new QuarterlyPlan(name, description, minimumDuration, pricePerMonth);
            case SEMI_ANNUAL -> new SemiAnnualPlan(name, description, minimumDuration, pricePerMonth);
            case ANNUAL -> new AnnualPlan(name, description, minimumDuration, pricePerMonth);
            default -> new MonthlyPlan(name, description, minimumDuration, pricePerMonth);
        };
    }

    /**
     * Busca um plano pelo nome (case-insensitive).
     */
    public OperationResult findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RequiredFieldException("nome do plano");
        }

        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                return new OperationResult(true, "Plano encontrado.", plan);
            }
        }

        return new OperationResult(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Atualiza o preço mensal de um plano.
     */
    public OperationResult updatePrice(String name, double newPrice) {
        if (name == null || name.trim().isEmpty()) {
            throw new RequiredFieldException("nome do plano");
        }
        if (newPrice <= 0) {
            return new OperationResult(false, "O novo preço deve ser um valor positivo.");
        }

        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                double oldPrice = plan.getPricePerMonth();
                plan.setPricePerMonth(newPrice);
                return new OperationResult(true,
                        "✅ Preço do plano \"" + plan.getName() + "\" atualizado de " +
                                CurrencyFormatter.formatCurrency(oldPrice) + " para " +
                                CurrencyFormatter.formatCurrency(newPrice) + ".", plan);
            }
        }

        return new OperationResult(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Lista todos os planos cadastrados.
     */
    public OperationResult listAll() {
        if (plans.isEmpty()) {
            return new OperationResult(false, "Nenhum plano cadastrado no sistema.");
        }

        return new OperationResult(true,
                plans.size() + " plano(s) encontrado(s).", new ArrayList<>(plans));
    }

    /**
     * Verifica se um nome de plano já existe (case-insensitive).
     */
    public boolean nameExists(String name) {
        for (Plan plan : plans) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
