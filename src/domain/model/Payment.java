package domain.model;

import domain.model.enums.PaymentType;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;

public class Payment {

    private static int nextCode = 1;

    private int code;
    private double amount;
    private LocalDate paymentDate;
    private PaymentType paymentType;
    private String description;

    public Payment(
            double amount,
            LocalDate paymentDate,
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
    // Getters e Setters
    // ========================

    public int getCode() {
        return code;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String toString() {
        return "Código: " + code + "\n" +
                "Valor: " + CurrencyFormatter.formatCurrency(amount) + "\n" +
                "Data: " + DateFormatter.format(paymentDate) + "\n" +
                "Tipo: " + paymentType.getLabel() + "\n" +
                "Descrição: " + description;
    }
}