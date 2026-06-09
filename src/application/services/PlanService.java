package application.services;

import application.OperationResult;
import domain.model.enums.PlanType;
import domain.model.plans.Plan;
import domain.model.plans.PlanFactory;
import exceptions.DuplicatedPlanException;
import exceptions.RequiredFieldException;
import persistence.PlanRepository;
import util.CurrencyFormatter;

import java.util.ArrayList;

/**
 * Serviço responsável pelas operações específicas da entidade Plan.
 *
 * Delega o armazenamento e a persistência da coleção ao {@link PlanRepository},
 * composto como atributo interno (relação de composição). O serviço cuida das
 * regras de negócio (validações de campo, unicidade de nome, instanciação da
 * subclasse concreta a partir do {@code PlanType}); o repositório cuida da
 * coleção e da persistência em arquivo.
 *
 * Política de comunicação de falhas:
 * - Campos obrigatórios vazios → {@link RequiredFieldException}.
 * - Nome de plano duplicado → {@link DuplicatedPlanException}.
 * - Demais validações (valores não positivos, plano não encontrado) seguem
 *   usando {@link OperationResult} com {@code success = false}.
 */
public class PlanService {

    private PlanRepository repository;

    public PlanService() {
        this.repository = new PlanRepository();
    }

    /**
     * Expõe o repositório composto.
     * Utilizado pelo orquestrador (FitManager) para coordenar persistência.
     */
    public PlanRepository getRepository() {
        return repository;
    }

    /**
     * Registra um novo plano no sistema.
     */
    public OperationResult<Plan> registerPlan(
            String name,
            String description,
            PlanType type,
            int minimumDuration,
            double pricePerMonth
    ) {
        if (name == null || name.isBlank()) {
            throw new RequiredFieldException("Nome do plano");
        }
        if (description == null || description.isBlank()) {
            throw new RequiredFieldException("Descrição do plano");
        }
        if (type == null) {
            throw new RequiredFieldException("Tipo do plano");
        }

        if (minimumDuration <= 0) {
            return new OperationResult<>(false, "A duração mínima deve ser maior que zero.");
        }
        if (pricePerMonth <= 0) {
            return new OperationResult<>(false, "O preço por mês deve ser um valor positivo.");
        }

        if (nameExists(name.trim())) {
            throw new DuplicatedPlanException(name.trim());
        }

        // Instancia a subclasse correta com base no tipo informado.
        // A decisão de qual subclasse criar reside em PlanFactory — reaproveitada
        // também pelo PlanRepository ao reconstruir planos da persistência.
        Plan plan = PlanFactory.create(
                type,
                name.trim(),
                description.trim(),
                minimumDuration,
                pricePerMonth
        );

        repository.add(plan);

        return new OperationResult<>(true,
                "✅ Plano \"" + plan.getName() + "\" registrado com sucesso!", plan);
    }

    /**
     * Busca um plano pelo nome (case-insensitive).
     */
    public OperationResult<Plan> findByName(String name) {
        if (name == null || name.isBlank()) {
            throw new RequiredFieldException("Nome do plano");
        }

        for (Plan plan : repository.listAll()) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                return new OperationResult<>(true, "Plano encontrado.", plan);
            }
        }

        return new OperationResult<>(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Atualiza o preço mensal de um plano.
     */
    public OperationResult<Plan> updatePrice(String name, double newPrice) {
        if (name == null || name.isBlank()) {
            throw new RequiredFieldException("Nome do plano");
        }
        if (newPrice <= 0) {
            return new OperationResult<>(false, "O novo preço deve ser um valor positivo.");
        }

        for (Plan plan : repository.listAll()) {
            if (plan.getName().equalsIgnoreCase(name.trim())) {
                double oldPrice = plan.getPricePerMonth();
                plan.setPricePerMonth(newPrice);
                return new OperationResult<>(true,
                        "✅ Preço do plano \"" + plan.getName() + "\" atualizado de " +
                                CurrencyFormatter.formatCurrency(oldPrice) + " para " +
                                CurrencyFormatter.formatCurrency(newPrice) + ".", plan);
            }
        }

        return new OperationResult<>(false, "Nenhum plano encontrado com o nome informado.");
    }

    /**
     * Lista todos os planos cadastrados.
     */
    public OperationResult<ArrayList<Plan>> listAll() {
        if (repository.isEmpty()) {
            return new OperationResult<>(false, "Nenhum plano cadastrado no sistema.");
        }

        return new OperationResult<>(true,
                repository.count() + " plano(s) encontrado(s).", repository.listAll());
    }

    /**
     * Verifica se um nome de plano já existe (case-insensitive).
     */
    public boolean nameExists(String name) {
        for (Plan plan : repository.listAll()) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
