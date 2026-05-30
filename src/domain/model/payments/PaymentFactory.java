package domain.model.payments;

import domain.model.enums.PaymentType;

import java.time.LocalDateTime;

/**
 * Factory estática centralizada para instanciar a subclasse correta de
 * {@link Payment} a partir do {@link PaymentType}.
 *
 * Mantém em um único ponto a decisão de qual subclasse concreta criar — usada
 * tanto pelo {@code EnrollmentService} (ao registrar um pagamento novo) quanto
 * pelo {@code EnrollmentRepository} (ao reconstruir os pagamentos a partir do
 * arquivo de persistência). Sem essa factory, o switch por tipo seria duplicado
 * entre as camadas de aplicação e persistência.
 *
 * Os dados específicos de cada subclasse vêm em {@code paymentData}:
 *  - {@code PIX}: [pixKey]
 *  - {@code CREDIT_CARD}: [installments, cardLastDigits]
 *  - {@code DEBIT_CARD}: [cardLastDigits]
 *  - {@code CASH}: [amountReceived]
 */
public final class PaymentFactory {

    private PaymentFactory() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Cria a subclasse concreta de {@link Payment} correspondente ao {@code type}.
     *
     * @param type        tipo do pagamento (define a subclasse concreta)
     * @param amount      valor do pagamento
     * @param paymentDate data e hora do pagamento
     * @param description descrição livre do pagamento
     * @param paymentData campos específicos do tipo (ver documentação da classe)
     * @return instância da subclasse concreta de {@link Payment}
     */
    public static Payment create(
            PaymentType type,
            double amount,
            LocalDateTime paymentDate,
            String description,
            String[] paymentData
    ) {
        switch (type) {
            case PIX:
                String pixKey = (paymentData != null && paymentData.length > 0 ? paymentData[0] : "");
                return new PixPayment(amount, paymentDate, description, pixKey);
            case CREDIT_CARD:
                int installments = 1;
                String creditDigits = "0000";
                if (paymentData != null && paymentData.length >= 2) {
                    installments = Integer.parseInt(paymentData[0]);
                    creditDigits = paymentData[1];
                }
                return new CreditCardPayment(amount, paymentDate, description, installments, creditDigits);
            case DEBIT_CARD:
                String debitDigits = (paymentData != null && paymentData.length > 0 ? paymentData[0] : "0000");
                return new DebitCardPayment(amount, paymentDate, description, debitDigits);
            case CASH:
                double amountReceived = amount;
                if (paymentData != null && paymentData.length > 0) {
                    amountReceived = Double.parseDouble(paymentData[0].replace(",", "."));
                }
                return new CashPayment(amount, paymentDate, description, amountReceived);
            default:
                return new PixPayment(amount, paymentDate, description, "");
        }
    }
}
