package domain.model.plans;

import domain.model.Enrollment;
import domain.model.enums.PlanType;
import util.CurrencyFormatter;

/**
 * Superclasse abstrata que representa um plano da academia.
 *
 * Define os atributos comuns a todos os planos (nome, descrição, tipo,
 * duração mínima, preço por mês) e declara os métodos abstratos que
 * cada subclasse deve implementar com sua própria lógica:
 * - calculateTotalPrice: regra de desconto específica por tipo
 * - getCancellationFee: taxa de cancelamento específica por tipo
 *
 * Não pode ser instanciada diretamente — use as subclasses concretas:
 * MonthlyPlan, QuarterlyPlan, SemiAnnualPlan, AnnualPlan.
 */
public abstract class Plan {

    private String name;
    private String description;
    private PlanType type;
    private int minimumDuration; // em meses
    private double pricePerMonth;

    protected Plan(
            String name,
            String description,
            PlanType type,
            int minimumDuration,
            double pricePerMonth
    ) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.minimumDuration = minimumDuration;
        this.pricePerMonth = pricePerMonth;
    }

    // ========================
    // Métodos abstratos
    // ========================

    /**
     * Calcula o preço total para uma determinada quantidade de meses.
     * Cada subclasse implementa sua própria regra de desconto.
     *
     * @param months quantidade de meses contratados (deve ser >= minimumDuration)
     * @return valor total calculado com desconto aplicado (se houver)
     */
    public abstract double calculateTotalPrice(int months);

    /**
     * Calcula a taxa de cancelamento para uma matrícula.
     * Cada subclasse define se aplica taxa e em quais condições.
     *
     * @param enrollment a matrícula sendo cancelada
     * @return valor da taxa de cancelamento (0.0 se não aplicável)
     */
    public abstract double getCancellationFee(Enrollment enrollment);

    // ========================
    // Métodos concretos
    // ========================

    /**
     * Retorna o nome amigável do tipo de plano para exibição ao usuário.
     * Utilizado em listagens e relatórios sem necessidade de instanceof.
     *
     * @return label do tipo de plano (ex: "Mensal", "Trimestral")
     */
    public String getTypeName() {
        return type.getLabel();
    }

    // ========================
    // Getters e Setters
    // ========================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PlanType getType() {
        return type;
    }

    public int getMinimumDuration() {
        return minimumDuration;
    }

    public void setMinimumDuration(int minimumDuration) {
        this.minimumDuration = minimumDuration;
    }

    public double getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(double pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    @Override
    public String toString() {
        return "Nome: " + name + "\n" +
                "Descrição: " + description + "\n" +
                "Tipo: " + type.getLabel() + "\n" +
                "Duração mínima: " + minimumDuration + (minimumDuration == 1 ? " mês" : " meses") + "\n" +
                "Preço/mês: " + CurrencyFormatter.formatCurrency(pricePerMonth);
    }
}
