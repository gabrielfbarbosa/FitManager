package application.services;

import application.OperationResult;
import domain.model.enums.PlanType;
import domain.model.plans.Plan;
import domain.model.plans.MonthlyPlan;
import domain.model.plans.QuarterlyPlan;
import domain.model.plans.SemiAnnualPlan;
import domain.model.plans.AnnualPlan;
import util.CurrencyFormatter;

import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de planos em memória
 * e implementar as operações específicas da entidade Plan.
 *
 * Conhece apenas objetos do seu próprio domínio.
 */
public class PlanService {

    private ArrayList<Plan> plans;

    public PlanService() {
        this.plans = new ArrayList<>();
    }

    /**
     * Registra um novo plano no sistema.
     * Valida campos obrigatórios, valores e unicidade de nome.
     *
     * @return OperationResult com o Plan criado em data (se sucesso)
     */
    public OperationResult registerPlan(
            String name,
            String description,
            PlanType type,
            int minimumDuration,
            double pricePerMonth
    ) {
        // Validação de campos obrigatórios
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome do plano é obrigatório.");
        }
        if (description == null || description.trim().isEmpty()) {
            return new OperationResult(false, "A descrição do plano é obrigatória.");
        }
        if (type == null) {
            return new OperationResult(false, "O tipo do plano é obrigatório.");
        }

        // Validação de valores
        if (minimumDuration <= 0) {
            return new OperationResult(false, "A duração mínima deve ser maior que zero.");
        }
        if (pricePerMonth <= 0) {
            return new OperationResult(false, "O preço por mês deve ser um valor positivo.");
        }

        // Verifica unicidade do nome
        if (nameExists(name.trim())) {
            return new OperationResult(false, "Já existe um plano cadastrado com este nome.");
        }

        // Instancia a subclasse correta com base no tipo informado.
        // Este é o ÚNICO ponto do sistema que conhece as subclasses concretas de Plan.
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
     * Centraliza a decisão de instanciação — nenhuma outra parte do sistema
     * precisa conhecer as subclasses concretas.
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
     * Busca um plano pelo nome (busca case-insensitive).
     *
     * @return OperationResult com o Plan encontrado em data (se sucesso)
     */
    public OperationResult findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome é obrigatório para consulta.");
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
     * Não afeta matrículas já registradas (totalPrice é fixado na Enrollment).
     *
     * @return OperationResult com o Plan atualizado em data (se sucesso)
     */
    public OperationResult updatePrice(String name, double newPrice) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome do plano é obrigatório.");
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
     *
     * @return OperationResult com ArrayList<Plan> em data
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
