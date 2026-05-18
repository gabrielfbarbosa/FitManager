package domain.model.filters;

import domain.model.Enrollment;
import domain.model.enums.EnrollmentStatus;

/**
 * Filtro que seleciona matrículas vencidas (data fim anterior a hoje)
 * que ainda estão com status ativo.
 */
public class ExpiredEnrollmentFilter implements EnrollmentFilter {

    @Override
    public boolean matches(Enrollment enrollment) {
        return enrollment.getStatus() == EnrollmentStatus.ACTIVE && enrollment.isExpired();
    }

    @Override
    public String getDescription() {
        return "Matrículas Vencidas";
    }
}
