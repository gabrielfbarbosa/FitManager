package domain.model.payments;

import domain.model.enums.PaymentType;
import util.DateFormatter;
import util.CurrencyFormatter;

import java.time.LocalDateTime;

/**
 * Pagamento via cartão de débito — sem taxa de processamento.
 * Atributo específico: últimos 4 dígitos do cartão.
 */
public class DebitCardPayment extends Payment {

    private String cardLastDigits;

    public DebitCardPayment(double amount, LocalDateTime paymentDate, String description, String cardLastDigits) {
        super(amount, paymentDate, PaymentType.DEBIT_CARD, description);

        this.cardLastDigits = cardLastDigits;
    }

    @Override
    public double getProcessingFee() {
        return 0.0;
    }

    @Override
    public String getPaymentSummary() {
        return "Pagamento #" + getCode() + " — Cartão de Débito\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(getAmount()) + "\n" +
                "Data: " + DateFormatter.formatDateTime(getPaymentDate()) + "\n" +
                "Cartão: **** **** **** " + cardLastDigits + "\n" +
                "Descrição: " + getDescription();
    }

    public String getCardLastDigits() {
        return cardLastDigits;
    }
}
