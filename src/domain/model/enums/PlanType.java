package domain.model.enums;

public enum PlanType {
    MONTHLY("Mensal"),
    QUARTERLY("Trimestral"),
    SEMI_ANNUAL("Semestral"),
    ANNUAL("Anual");

    private final String label;

    PlanType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return label;
    }
}