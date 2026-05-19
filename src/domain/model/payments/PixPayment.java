package domain.model.payments;

import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDateTime;

/**
 * Pagamento via PIX — sem taxa de processamento.
 * Atributo específico: chave PIX utilizada na transação.
 */
public class PixPayment extends Payment {

    private String pixKey;

    public PixPayment(double amount, LocalDateTime paymentDate, String description, String pixKey) {
        super(amount, paymentDate, PaymentType.PIX, description);

        this.pixKey = pixKey;
    }

    @Override
    public double getProcessingFee() {
        return 0.0;
    }

    @Override
    public String getPaymentSummary() {
        return "Pagamento #" + getCode() + " — PIX\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(getAmount()) + "\n" +
                "Data: " + DateFormatter.formatDateTime(getPaymentDate()) + "\n" +
                "Chave PIX: " + pixKey + "\n" +
                "Descrição: " + getDescription();
    }

    public String getPixKey() {
        return pixKey;
    }
}
