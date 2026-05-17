package domain.model.filters;

import domain.model.Enrollment;
import domain.model.enums.EnrollmentStatus;

/**
 * Filtro que seleciona apenas matrículas com status ACTIVE.
 */
public class ActiveEnrollmentFilter implements EnrollmentFilter {

    @Override
    public boolean matches(Enrollment enrollment) {
        return enrollment.getStatus() == EnrollmentStatus.ACTIVE;
    }

    @Override
    public String getDescription() {
        return "Matrículas Ativas";
    }
}
