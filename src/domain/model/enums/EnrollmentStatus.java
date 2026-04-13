package domain.model.enums;

public enum EnrollmentStatus {
    ACTIVE("Ativa"),
    CANCELLED("Cancelada");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
