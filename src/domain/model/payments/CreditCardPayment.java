package domain.model.payments;

import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDateTime;

/**
 * Pagamento via cartão de crédito — taxa de processamento de 2,5%.
 * Política: absorção pela academia — o aluno paga o valor nominal,
 * mas apenas (amount - taxa) é efetivamente creditado na matrícula.
 *
 * Atributos específicos: número de parcelas e últimos 4 dígitos do cartão.
 */
public class CreditCardPayment extends Payment {

    private static final double PROCESSING_FEE_RATE = 0.025;

    private int installments;
    private String cardLastDigits;

    public CreditCardPayment(double amount, LocalDateTime paymentDate, String description, int installments, String cardLastDigits) {
        super(amount, paymentDate, PaymentType.CREDIT_CARD, description);

        this.installments = installments;
        this.cardLastDigits = cardLastDigits;
    }

    @Override
    public double getProcessingFee() {
        return getAmount() * PROCESSING_FEE_RATE;
    }

    @Override
    public String getPaymentSummary() {
        return "Pagamento #" + getCode() + " — Cartão de Crédito\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(getAmount()) + "\n" +
                "Data: " + DateFormatter.formatDateTime(getPaymentDate()) + "\n" +
                "Cartão: **** **** **** " + cardLastDigits + "\n" +
                "Parcelas: " + installments + "x de " +
                CurrencyFormatter.formatCurrency(getAmount() / installments) + "\n" +
                "Taxa de processamento: " + CurrencyFormatter.formatCurrency(getProcessingFee()) + "\n" +
                "Valor creditado: " + CurrencyFormatter.formatCurrency(getEffectiveAmount()) + "\n" +
                "Descrição: " + getDescription();
    }

    /** Ordem de leitura cartão de crédito: {@code [installments, cardLastDigits]}. */
    @Override
    public String[] getCsvExtraFields() {
        return new String[]{String.valueOf(installments), cardLastDigits};
    }
}
