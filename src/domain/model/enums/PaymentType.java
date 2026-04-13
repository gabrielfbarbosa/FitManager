package domain.model.enums;

public enum PaymentType {
    PIX("PIX"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    CASH("Dinheiro");

    private final String label;

    PaymentType(String label) {
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
