package domain.model;

import domain.model.enums.PlanType;

public class Plan {
    private static final double DISCOUNT_RATE = 0.10; // 10% de desconto nos meses excedentes

    private String name;
    private String description;
    private PlanType type;
    private int minimumDuration; // em meses
    private double pricePerMonth;

    public Plan(
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
    // Métodos de negócio
    // ========================

    /**
     * Calcula o preço total para uma determinada quantidade de meses.
     * Aplica desconto de 10% nos meses que excedem a duração mínima do plano.
     *
     * Exemplo: Plano mensal (min. 1 mês, R$100/mês) contratado por 12 meses:
     * 1 × R$100 + 11 × R$90 = R$1.090,00
     *
     * @param months quantidade de meses contratados
     * @return valor total calculado
     */
    public double calculateTotalPrice(int months) {
        if (months <= 0) {
            return 0;
        }

        if (months <= minimumDuration) {
            return months * pricePerMonth;
        }

        // Meses dentro da duração mínima: preço cheio
        double basePart = minimumDuration * pricePerMonth;
        // Meses excedentes: preço com desconto
        int extraMonths = months - minimumDuration;
        double discountedPrice = pricePerMonth * (1 - DISCOUNT_RATE);
        double extraPart = extraMonths * discountedPrice;

        return basePart + extraPart;
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

    public void setType(PlanType type) {
        this.type = type;
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
                "Preço/mês: R$ " + String.format("%.2f", pricePerMonth);
    }
}
