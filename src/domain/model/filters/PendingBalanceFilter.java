package domain.model.filters;

import domain.model.Enrollment;

/**
 * Filtro que seleciona matrículas com saldo pendente (não totalmente pagas).
 */
public class PendingBalanceFilter implements EnrollmentFilter {

    @Override
    public boolean matches(Enrollment enrollment) {
        return enrollment.calculateBalance() > 0;
    }

    @Override
    public String getDescription() {
        return "Matrículas com Saldo Pendente";
    }
}
