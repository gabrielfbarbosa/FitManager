package domain.model.payments;

import domain.model.Summarizable;
import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Payment implements Summarizable {

    private static int nextCode = 1;

    private int code;
    private double amount;
    private LocalDateTime paymentDate;
    private PaymentType paymentType;
    private String description;

    protected Payment(
            double amount,
            LocalDateTime paymentDate,
            PaymentType paymentType,
            String description
    ) {
        this.code = nextCode++;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentType = paymentType;
        this.description = description;
    }

    // ========================
    // Métodos abstratos
    // ========================

    /**
     * Retorna a taxa de processamento deste meio de pagamento.
     * Política: absorção pela academia — o aluno paga o valor nominal,
     * mas o valor efetivamente creditado é (amount - fee).
     *
     * @return valor da taxa de processamento (0.0 se não aplicável)
     */
    public abstract double getProcessingFee();

    /**
     * Retorna um resumo textual do pagamento com as informações
     * relevantes para o tipo específico.
     *
     * @return String formatada com os dados do pagamento
     */
    public abstract String getPaymentSummary();

    // ========================
    // Métodos concretos
    // ========================

    /**
     * Retorna um resumo curto do pagamento em uma única linha.
     * Formato: "Pagamento #X | R$ XX,XX | Tipo | dd/MM/yyyy"
     */
    @Override
    public String getSummary() {
        return "Pagamento Código: " + code + " | " + CurrencyFormatter.formatCurrency(amount) +
                " | " + paymentType.getLabel() + " | " + DateFormatter.formatDateTime(paymentDate);
    }

    /**
     * Retorna o valor efetivamente creditado na matrícula,
     * descontando a taxa de processamento (absorvida pela academia).
     *
     * @return amount - getProcessingFee()
     */
    public double getEffectiveAmount() {
        return amount - getProcessingFee();
    }

    /**
     * Retorna o nome amigável do tipo de pagamento para exibição.
     * Utilizado em listagens e relatórios sem necessidade de instanceof.
     *
     * @return label do tipo de pagamento (ex: "PIX", "Cartão de Crédito")
     */
    public String getTypeName(){
        return paymentType.getLabel();
    }

    // ========================
    // Getters
    // ========================

    public int getCode() {
        return code;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }


    public String getDescription() {
        return description;
    }

    public String toString() {
        return "Código: " + code + "\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(amount) + "\n" +
                "Data: " + DateFormatter.formatDateTime(paymentDate) + "\n" +
                "Tipo: " + paymentType.getLabel() + "\n" +
                "Descrição: " + description;
    }
}