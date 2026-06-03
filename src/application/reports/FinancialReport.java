package application.reports;

import domain.model.payments.Payment;
import util.CurrencyFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resultado consolidado de um relatório financeiro mensal.
 *
 * Representa um relatório já calculado para um mês/ano específico —
 * não é um service que calcula, é um objeto que armazena as métricas
 * resultantes. Quem coordena o cálculo é o {@code FitManager}, conforme
 * a separação de responsabilidades já estabelecida.
 *
 * Métricas (todas exigidas pelo enunciado):
 *  - Receita total do período (soma dos pagamentos).
 *  - Receita por tipo de plano — agrupada via {@code plan.getTypeName()},
 *    sem condicionais por subclasse concreta.
 *  - Receita por forma de pagamento — agrupada via {@code payment.getTypeName()}.
 *  - Total de taxas de processamento — soma de {@code payment.getProcessingFee()}.
 *  - Matrículas iniciadas e canceladas no período.
 *  - Tipos de plano mais contratados — contagem por tipo, ordem decrescente.
 *
 * Polimorfismo: a agregação por tipo de plano e por forma de pagamento
 * usa {@code getTypeName()} de {@link domain.model.plans.Plan} e
 * {@link Payment} — sem {@code instanceof} nem {@code getClass()}. O
 * relatório opera sobre os tipos abstratos e deixa cada subclasse se
 * identificar.
 *
 * Período sem dados é um resultado válido: o objeto é retornado com
 * todas as métricas zeradas e {@link #format()} indica isso na exibição.
 *
 * Sabe exibir-se ({@link #format()}) e exportar-se ({@link #toCsv()}).
 */
public class FinancialReport {

    private final int month;
    private final int year;

    private double totalRevenue;
    private double totalProcessingFees;
    private int enrollmentsStarted;
    private int enrollmentsCancelled;

    private final Map<String, Double> revenueByPlanType = new LinkedHashMap<>();
    private final Map<String, Double> revenueByPaymentType = new LinkedHashMap<>();
    private final Map<String, Integer> enrollmentsByPlanType = new LinkedHashMap<>();

    public FinancialReport(int month, int year) {
        this.month = month;
        this.year = year;
    }

    /**
     * Agrega um pagamento ao relatório, somando ao total e aos
     * agrupamentos por tipo de plano e forma de pagamento. A taxa de
     * processamento é acumulada via {@link Payment#getProcessingFee()}.
     */
    public void addPayment(String planTypeName, Payment payment) {
        double amount = payment.getAmount();
        totalRevenue += amount;
        totalProcessingFees += payment.getProcessingFee();
        revenueByPlanType.merge(planTypeName, amount, Double::sum);
        revenueByPaymentType.merge(payment.getTypeName(), amount, Double::sum);
    }

    /**
     * Registra uma matrícula iniciada no período, incrementando o total
     * e a contagem por tipo de plano (para o ranking de mais contratados).
     */
    public void addEnrollmentStarted(String planTypeName) {
        enrollmentsStarted++;
        enrollmentsByPlanType.merge(planTypeName, 1, Integer::sum);
    }

    /**
     * Registra uma matrícula cancelada no período.
     */
    public void incrementCancelled() {
        enrollmentsCancelled++;
    }

    // ============================
    // Getters
    // ============================

    public int getMonth() { return month; }
    public int getYear() { return year; }

    /**
     * Indica se o período não teve nenhuma atividade financeira.
     * Usado para apresentar uma mensagem informativa em vez de
     * tratar como erro.
     */
    public boolean isEmpty() {
        return totalRevenue == 0 && enrollmentsStarted == 0 && enrollmentsCancelled == 0;
    }

    // ============================
    // Formatação
    // ============================

    /**
     * Devolve uma representação textual completa do relatório para
     * exibição ao usuário (uso em {@code showScrollableMessage}).
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("📊 RELATÓRIO FINANCEIRO MENSAL — ")
          .append(String.format("%02d/%d", month, year)).append("\n");
        sb.append("══════════════════════════════════════════\n\n");

        sb.append("Receita Total do Período: ")
          .append(CurrencyFormatter.formatCurrency(totalRevenue)).append("\n");
        sb.append("Total de Taxas de Processamento: ")
          .append(CurrencyFormatter.formatCurrency(totalProcessingFees)).append("\n\n");

        sb.append("Receita por Tipo de Plano:\n");
        appendRevenueMap(sb, revenueByPlanType);
        sb.append("\n");

        sb.append("Receita por Forma de Pagamento:\n");
        appendRevenueMap(sb, revenueByPaymentType);
        sb.append("\n");

        sb.append("Matrículas iniciadas no período: ").append(enrollmentsStarted).append("\n");
        sb.append("Matrículas canceladas no período: ").append(enrollmentsCancelled).append("\n\n");

        sb.append("Tipos de plano mais contratados no período:\n");
        if (enrollmentsByPlanType.isEmpty()) {
            sb.append("  (nenhuma matrícula iniciada no período)\n");
        } else {
            List<Map.Entry<String, Integer>> sorted = new ArrayList<>(enrollmentsByPlanType.entrySet());
            sorted.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());
            for (Map.Entry<String, Integer> e : sorted) {
                sb.append("  - ").append(e.getKey()).append(": ")
                  .append(e.getValue()).append(" matrícula(s)\n");
            }
        }

        if (isEmpty()) {
            sb.append("\nℹ️ Nenhuma atividade financeira encontrada no período informado.\n");
        }

        return sb.toString();
    }

    private void appendRevenueMap(StringBuilder sb, Map<String, Double> map) {
        if (map.isEmpty()) {
            sb.append("  (sem receita no período)\n");
            return;
        }
        for (Map.Entry<String, Double> e : map.entrySet()) {
            sb.append("  - ").append(e.getKey()).append(": ")
              .append(CurrencyFormatter.formatCurrency(e.getValue())).append("\n");
        }
    }

    /**
     * Serializa o relatório em formato CSV com {@code ;} como delimitador
     * (mesmo padrão da persistência principal do sistema), pronto para
     * exportação a planilhas.
     */
    public String toCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("FitManager - Relatório Financeiro Mensal\n");
        sb.append("Período;").append(String.format("%02d/%d", month, year)).append("\n");

        sb.append("Receita Total;").append(CurrencyFormatter.formatDecimal(totalRevenue)).append("\n");
        sb.append("Taxas de Processamento;").append(CurrencyFormatter.formatDecimal(totalProcessingFees)).append("\n");
        sb.append("Matrículas Iniciadas;").append(enrollmentsStarted).append("\n");
        sb.append("Matrículas Canceladas;").append(enrollmentsCancelled).append("\n");

        sb.append("\nReceita por Tipo de Plano\n");
        sb.append("Tipo;Valor\n");
        for (Map.Entry<String, Double> e : revenueByPlanType.entrySet()) {
            sb.append(e.getKey()).append(";")
                    .append(CurrencyFormatter.formatDecimal((e.getValue()))).append("\n");
        }

        sb.append("\nReceita por Forma de Pagamento\n");
        sb.append("Forma;Valor\n");
        for (Map.Entry<String, Double> e : revenueByPaymentType.entrySet()) {
            sb.append(e.getKey()).append(";")
                    .append(CurrencyFormatter.formatDecimal((e.getValue()))).append("\n");
        }

        sb.append("\nTipos de Plano Mais Contratados\n");
        sb.append("Tipo;Quantidade\n");
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(enrollmentsByPlanType.entrySet());
        sorted.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue).reversed());
        for (Map.Entry<String, Integer> e : sorted) {
            sb.append(e.getKey()).append(";").append(e.getValue()).append("\n");
        }

        return sb.toString();
    }
}
