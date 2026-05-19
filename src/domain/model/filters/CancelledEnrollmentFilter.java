package domain.model.filters;

import domain.model.Enrollment;
import domain.model.enums.EnrollmentStatus;

/**
 * Filtro que seleciona apenas matrículas com status CANCELLED.
 */
public class CancelledEnrollmentFilter implements EnrollmentFilter {

    @Override
    public boolean matches(Enrollment enrollment) {
        return enrollment.getStatus() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public String getDescription() {
        return "Matrículas Canceladas";
    }
}
