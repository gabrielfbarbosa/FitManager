package domain.model.filters;

import domain.model.Enrollment;
import domain.model.enums.PlanType;

/**
 * Filtro que seleciona matrículas cujo plano é de um tipo específico.
 *
 * Demonstra filtro parametrizado: o tipo de plano é definido no construtor,
 * permitindo reutilizar a mesma classe para filtrar por qualquer PlanType.
 */
public class ByPlanTypeFilter implements EnrollmentFilter {

    private PlanType planType;

    public ByPlanTypeFilter(PlanType planType) {
        this.planType = planType;
    }

    @Override
    public boolean matches(Enrollment enrollment) {
        return enrollment.getPlan().getType() == planType;
    }

    @Override
    public String getDescription() {
        return "Matrículas do Plano: " + planType.getLabel();
    }
}
