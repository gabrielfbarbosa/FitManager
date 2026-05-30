package persistence;

import domain.model.plans.Plan;
import exceptions.PersistenceException;

/**
 * Repositório concreto para planos.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Plan>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Plan}.
 *
 * A coleção é heterogênea: armazena instâncias das subclasses concretas
 * ({@code MonthlyPlan}, {@code QuarterlyPlan}, {@code SemiAnnualPlan},
 * {@code AnnualPlan}). A persistência deve preservar o tipo concreto de
 * cada elemento para que o polimorfismo continue funcionando após a
 * recarga — esse aspecto é tratado futuramente.
 *
 * Usado por composição em {@code PlanService}.
 *
 * As implementações de {@link #save()} e {@link #load()} são preenchidas
 * futuramente (persistência em arquivos texto). Por enquanto, são no-op.
 */
public class PlanRepository extends Repository<Plan> {

    /**
     * Persiste a coleção de planos em arquivo, preservando o tipo
     * concreto de cada subclasse de {@link Plan}.
     * Implementação concreta futura.
     *
     * @throws PersistenceException se ocorrer falha de escrita
     */
    @Override
    public void save() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }

    /**
     * Recupera a coleção de planos a partir do arquivo, reconstituindo
     * cada elemento com a subclasse concreta correta de {@link Plan}.
     * Implementação concreta futura.
     *
     * @throws PersistenceException se ocorrer falha de leitura ou
     *         se o arquivo estiver corrompido
     */
    @Override
    public void load() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }
}
