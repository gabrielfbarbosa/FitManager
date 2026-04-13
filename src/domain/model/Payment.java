package domain.model;

import domain.model.enums.PaymentType;

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

    @Override
    public String toString() {
        return "Código: " + code + "\n" +
                "Valor: R$ " + String.format("%.2f", amount) + "\n" +
                "Data: " + String.format("%02d/%02d/%04d", paymentDate.getDayOfMonth(),
                paymentDate.getMonthValue(), paymentDate.getYear()) + "\n" +
                "Tipo: " + paymentType.getLabel() + "\n" +
                "Descrição: " + description;
    }
}