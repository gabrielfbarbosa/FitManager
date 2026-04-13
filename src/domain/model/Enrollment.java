package domain.model;

import domain.model.enums.EnrollmentStatus;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class Enrollment {

    private static int nextCode = 1;

    private int code;
    private String studentCpf;
    private String planName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMonths;
    private double totalPrice;
    private EnrollmentStatus status;
    private ArrayList<Payment> payments;
    private LocalDate cancelledAt;

    public Enrollment(
            String studentCpf,
            String planName,
            LocalDate startDate,
            int durationMonths,
            double totalPrice
    ) {
        this.code = nextCode++;
        this.studentCpf = studentCpf;
        this.planName = planName;
        this.startDate = startDate;
        this.durationMonths = durationMonths;
        this.endDate = startDate.plusMonths(durationMonths).minusDays(1);
        this.totalPrice = totalPrice;
        this.status = EnrollmentStatus.ACTIVE;
        this.payments = new ArrayList<>();
        this.cancelledAt = null;
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Adiciona um pagamento à matrícula.
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    /**
     * Cancela a matrícula (soft delete).
     */
    public void cancel() {
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = LocalDate.now();
    }

    /**
     * Calcula o saldo pendente da matrícula.
     * Saldo = totalPrice - soma de todos os pagamentos.
     */
    public double calculateBalance() {
        double totalPaid = 0;
        for (Payment payment : payments) {
            totalPaid += payment.getAmount();
        }
        return totalPrice - totalPaid;
    }

    /**
     * Verifica se a matrícula está vencida (hoje é depois da endDate).
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Calcula quantos meses faltam para o fim da matrícula.
     * Retorna 0 se já está vencida ou cancelada.
     */
    public int getMonthsRemaining() {
        if (status == EnrollmentStatus.CANCELLED || isExpired()) {
            return 0;
        }
        YearMonth today = YearMonth.now();
        YearMonth end = YearMonth.from(endDate);
        int months = 0;
        YearMonth current = today;
        while (current.isBefore(end)) {
            months++;
            current = current.plusMonths(1);
        }
        return months;
    }

    // ========================
    // Getters e Setters
    // ========================

    public int getCode() {
        return code;
    }

    public String getStudentCpf() {
        return studentCpf;
    }

    public String getPlanName() {
        return planName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public ArrayList<Payment> getPayments() {
        return new ArrayList<>(payments);
    }

    public LocalDate getCancelledAt() {
        return cancelledAt;
    }

    @Override
    public String toString() {
        String formatDate = "%02d/%02d/%04d";
        return "Código: " + code + "\n" +
                "CPF: " + studentCpf + "\n" +
                "Plano: " + planName + "\n" +
                "Data Início: " + String.format(formatDate,
                startDate.getDayOfMonth(), startDate.getMonthValue(), startDate.getYear()) + "\n" +
                "Data Fim: " + String.format(formatDate,
                endDate.getDayOfMonth(), endDate.getMonthValue(), endDate.getYear()) + "\n" +
                "Duração: " + durationMonths + (durationMonths == 1 ? " mês" : " meses") + "\n" +
                "Preço Total: R$ " + String.format("%.2f", totalPrice) + "\n" +
                "Saldo Pendente: R$ " + String.format("%.2f", calculateBalance()) + "\n" +
                "Status: " + status.getLabel() + "\n" +
                "Pagamentos: " + payments.size();
    }
}