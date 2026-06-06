package domain.model.payments;

import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDateTime;

/**
 * Pagamento em dinheiro — sem taxa de processamento.
 * Atributo específico: valor entregue pelo aluno (deve ser >= amount).
 * Método exclusivo: getChange() retorna o troco.
 */
public class CashPayment extends Payment {

    private double amountReceived;

    public CashPayment(double amount, LocalDateTime paymentDate, String description, double amountReceive) {
        super(amount, paymentDate, PaymentType.CASH, description);

        this.amountReceived = amountReceive;
    }

    @Override
    public double getProcessingFee() {
        return 0.0;
    }

    /**
     * Calcula o troco a ser devolvido ao aluno.
     *
     * @return amountReceived - amount (sempre >= 0)
     */
    public double getChange(){
        return amountReceived - getAmount();
    }

    @Override
    public String getPaymentSummary() {
        String summary = "Pagamento #" + getCode() + " — Dinheiro\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(getAmount()) + "\n" +
                "Data: " + DateFormatter.formatDateTime(getPaymentDate()) + "\n" +
                "Valor recebido: " + CurrencyFormatter.formatCurrency(amountReceived) + "\n";

        double change = getChange();
        if (change > 0) {
            summary += "Troco: " + CurrencyFormatter.formatCurrency(change) + "\n";
        }

        summary += "Descrição: " + getDescription();
        return summary;
    }

    /** Ordem de leitura dinheiro: {@code [amountReceived]}. */
    @Override
    public String[] getCsvExtraFields() {
        return new String[]{String.valueOf(amountReceived)};
    }
}
