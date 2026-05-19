package domain.model.plans;

import domain.model.Enrollment;
import domain.model.enums.PlanType;

import java.time.LocalDate;

/**
 * Plano anual — desconto de 15% quando months > minDurationMonths.
 * Taxa de cancelamento de 20% do totalPrice se cancelado antes da metade do período.
 *
 * Regra de cálculo:
 * - Se months <= minDurationMonths: pricePerMonth × months (sem desconto)
 * - Se months > minDurationMonths: (pricePerMonth × months) × 0.85 (15% sobre o total bruto)
 *
 * Regra de cancelamento:
 * - Se o cancelamento ocorre ANTES da metade do período contratado: 20% do totalPrice
 * - Se ocorre NA metade ou APÓS: taxa zero (isenção)
 * - A taxa é informativa — não afeta calculateBalance()
 */
public class AnnualPlan extends Plan {

    private static final double DISCOUNT_RATE = 0.15;
    private static final double CANCELLATION_FEE_RATE = 0.20;

    public AnnualPlan(
        String name,
        String description,
        int minimumDuration,
        double pricePerMonth
    ) {
        super(
            name,
            description,
            PlanType.ANNUAL,
            minimumDuration,
            pricePerMonth
        );
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

    /**
     * Calcula a taxa de cancelamento para matrícula em plano anual.
     *
     * A taxa de 20% do totalPrice é aplicada apenas se o cancelamento
     * ocorrer antes de completar metade do período contratado.
     * Após a metade (inclusive), a taxa é zero.
     *
     * Exemplo: matrícula de 12 meses iniciada em 01/01/2026
     * - Metade do período = 6 meses → 01/07/2026
     * - Cancelamento em 15/06/2026 → ANTES da metade → taxa de 20%
     * - Cancelamento em 01/07/2026 → NA metade → isenção (taxa zero)
     * - Cancelamento em 15/08/2026 → APÓS metade → isenção (taxa zero)
     */
    @Override
    public double getCancellationFee(Enrollment enrollment) {
        LocalDate startDate = enrollment.getStartDate();
        int durationMonths = enrollment.getDurationMonths();

        // Calcula a data que marca a metade do período contratado
        LocalDate halfwayDate = startDate.plusMonths(durationMonths / 2);

        LocalDate today = LocalDate.now();

        // Se o cancelamento ocorre ANTES da metade do período, aplica taxa
        if (today.isBefore(halfwayDate)) {
            return enrollment.getTotalPrice() * CANCELLATION_FEE_RATE;
        }

        // Na metade ou após: isenção
        return 0.0;
    }
}
