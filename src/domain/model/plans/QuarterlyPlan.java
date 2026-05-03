package domain.model.plans;

import domain.model.Enrollment;
import domain.model.enums.PlanType;

/**
 * Plano trimestral — desconto de 5% quando months > minDurationMonths.
 * Sem taxa de cancelamento.
 *
 * Regra de cálculo:
 * - Se months <= minDurationMonths: pricePerMonth × months (sem desconto)
 * - Se months > minDurationMonths: (pricePerMonth × months) × 0.95 (5% sobre o total bruto)
 */
public class QuarterlyPlan extends Plan {

    private static final double DISCOUNT_RATE = 0.05;

    public QuarterlyPlan(String name, String description, int minimumDuration, double pricePerMonth) {
        super(name, description, PlanType.QUARTERLY, minimumDuration, pricePerMonth);
    }

    @Override
    public double calculateTotalPrice(int months) {
        if (months <= 0) {
            return 0;
        }

        double grossTotal = getPricePerMonth() * months;

        if (months > getMinimumDuration()) {
            return grossTotal * (1 - DISCOUNT_RATE);
        }

        return grossTotal;
    }

    @Override
    public double getCancellationFee(Enrollment enrollment) {
        return 0.0;
    }
}
