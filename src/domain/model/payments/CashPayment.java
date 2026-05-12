package domain.model.payments;

import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;

/**
 * Pagamento em dinheiro — sem taxa de processamento.
 * Atributo específico: valor entregue pelo aluno (deve ser >= amount).
 * Método exclusivo: getChange() retorna o troco.
 */
public class CashPayment extends Payment {

    private double amountReceived;

    public CashPayment(double amount, LocalDate paymentDate, String description, double amountReceive) {
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
                "Data: " + DateFormatter.format(getPaymentDate()) + "\n" +
                "Valor recebido: " + CurrencyFormatter.formatCurrency(amountReceived) + "\n";

        double change = getChange();
        if (change > 0) {
            summary += "Troco: " + CurrencyFormatter.formatCurrency(change) + "\n";
        }

        summary += "Descrição: " + getDescription();
        return summary;
    }

    public double getAmountReceived() {
        return amountReceived;
    }
}
