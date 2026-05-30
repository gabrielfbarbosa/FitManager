package domain.model.plans;

import domain.model.enums.PlanType;

/**
 * Factory estática centralizada para instanciar a subclasse correta de {@link Plan}
 * a partir do {@link PlanType}.
 *
 * Mantém em um único ponto a decisão de qual subclasse concreta criar — usado
 * tanto pelo {@code PlanService} (ao cadastrar um plano novo) quanto pelo
 * {@code PlanRepository} (ao reconstruir a coleção a partir do arquivo de
 * persistência). Sem essa factory, o switch por tipo seria duplicado entre as
 * camadas de aplicação e persistência.
 *
 * Esse é um dos poucos lugares do sistema onde conhecer os tipos concretos é
 * inevitável — o objeto ainda não existe e precisa ser criado.
 */
public final class PlanFactory {

    private PlanFactory() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Cria a subclasse concreta de {@link Plan} correspondente ao {@code type}.
     *
     * @param type             tipo do plano (define a subclasse concreta)
     * @param name             nome do plano
     * @param description      descrição do plano
     * @param minimumDuration  duração mínima em meses
     * @param pricePerMonth    preço mensal
     * @return instância da subclasse concreta de {@link Plan}
     */
    public static Plan create(
            PlanType type,
            String name,
            String description,
            int minimumDuration,
            double pricePerMonth
    ) {
        return switch (type) {
            case MONTHLY -> new MonthlyPlan(name, description, minimumDuration, pricePerMonth);
            case QUARTERLY -> new QuarterlyPlan(name, description, minimumDuration, pricePerMonth);
            case SEMI_ANNUAL -> new SemiAnnualPlan(name, description, minimumDuration, pricePerMonth);
            case ANNUAL -> new AnnualPlan(name, description, minimumDuration, pricePerMonth);
        };
    }
}
