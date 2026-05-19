package domain.model;

import domain.model.enums.EnrollmentStatus;
import domain.model.payments.Payment;
import domain.model.plans.Plan;
import util.CurrencyFormatter;
import util.DateFormatter;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;

public class Enrollment implements Summarizable {

    private static int nextCode = 1;

    private int code;
    private String studentCpf;
    private String planName;
    private Plan plan;
    private LocalDate startDate;
    private LocalDate endDate;
    private int durationMonths;
    private double totalPrice;
    private EnrollmentStatus status;
    private ArrayList<Payment> payments;
    private LocalDate cancelledAt;

    public Enrollment(
            String studentCpf,
            Plan plan,
            LocalDate startDate,
            int durationMonths,
            double totalPrice
    ) {
        this.code = nextCode++;
        this.studentCpf = studentCpf;
        this.plan = plan;
        this.planName = plan.getName();
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
     * Retorna um resumo curto da matrícula em uma única linha.
     * Formato: "Matrícula #X | Plano Y | Status | Saldo: R$ XX,XX"
     */
    @Override
    public String getSummary() {
        return "Matrícula Código: " + code + " | " + planName + " | " +
                status.getLabel() + " | Saldo Pendente: " + CurrencyFormatter.formatCurrency(calculateBalance());

    }

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

    public Plan getPlan() {
        return plan;
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
        return "Código: " + code + "\n" +
                "CPF: " + studentCpf + "\n" +
                "Plano: " + planName + "\n" +
                "Data Início: " + DateFormatter.formatDate(startDate) + "\n" +
                "Data Fim: " + DateFormatter.formatDate(endDate) + "\n" +
                "Duração: " + durationMonths + (durationMonths == 1 ? " mês" : " meses") + "\n" +
                "Preço Total: " + CurrencyFormatter.formatCurrency(totalPrice) + "\n" +
                "Saldo Pendente: " + CurrencyFormatter.formatCurrency(calculateBalance()) + "\n" +
                "Status: " + status.getLabel() + "\n" +
                "Pagamentos: " + payments.size();
    }
}