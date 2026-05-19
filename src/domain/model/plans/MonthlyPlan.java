package domain.model.plans;

import domain.model.Enrollment;
import domain.model.enums.PlanType;

/**
 * Plano mensal — sem desconto em nenhuma situação.
 * Sem taxa de cancelamento.
 *
 * Regra de cálculo: pricePerMonth × months (valor bruto, sem desconto).
 */
public class MonthlyPlan extends Plan {

    public MonthlyPlan(String name, String description, int minimumDuration, double pricePerMonth) {
        super(name, description, PlanType.MONTHLY, minimumDuration, pricePerMonth);
    }

    @Override
    public double calculateTotalPrice(int months) {
        if (months <= 0) {
            return 0;
        }
        return getPricePerMonth() * months;
    }

    @Override
    public double getCancellationFee(Enrollment enrollment) {
        return 0.0;
    }
}
