package domain.model;

import java.time.LocalDate;
import java.time.Period;

import util.DateFormatter;

public class Student {

    private String name;
    private String cpf;
    private String contact;
    private LocalDate birthDate;
    private boolean active;
    private LocalDate removedAt;

    public Student(
            String name,
            String cpf,
            String contact,
            LocalDate birthDate
    ) {
        this.name = name;
        this.cpf = cpf;
        this.contact = contact;
        this.birthDate = birthDate;
        this.active = true;
        this.removedAt = null;
    }

    // ========================
    // Métodos de negócio
    // ========================

    /**
     * Calcula a idade do aluno a partir da data de nascimento.
     * Evita armazenar um dado que pode sempre ser derivado.
     */
    public int calculateAge() {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Ativa o aluno no sistema.
     */
    public void activate() {
        this.active = true;
        this.removedAt = null;
    }

    /**
     * Desativa o aluno (remoção lógica).
     * Registra a data de remoção para preservar histórico.
     */
    public void deactivate() {
        this.active = false;
        this.removedAt = LocalDate.now();
    }

    /**
     * Remove todos os caracteres não numéricos de uma string.
     *
     * @param value string de entrada (pode conter pontos, traços, espaços etc.)
     * @return string contendo apenas os dígitos numéricos
     */
    public static String cleanCpf(String value) {
        if (value == null) {
            return "";
        }

        char[] digits = new char[value.length()];
        int count = 0;

        for (char c : value.toCharArray()) {
            if (c >= '0' && c <= '9') {
                digits[count] = c;
                count++;
            }
        }

        return new String(digits, 0, count);
    }

    /**
     * Verifica se todos os dígitos de um CPF (já limpo) são iguais.
     * CPFs como "111.111.111-11" são estruturalmente inválidos.
     *
     * @param cpf string com exatamente 11 dígitos numéricos
     * @return true se todos os dígitos forem iguais
     */
    private static boolean allDigitsEqual(String cpf) {
        for (int i = 1; i < 11; i++) {
            if (cpf.charAt(i) != cpf.charAt(0)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calcula um dígito verificador do CPF pelo algoritmo módulo 11.
     *
     * @param cpf           string com exatamente 11 dígitos numéricos
     * @param digitPosition posição do dígito a calcular: 9 para o primeiro, 10 para o segundo
     * @return dígito verificador esperado (0–9)
     */
    private static int calculateVerifierDigit(String cpf, int digitPosition) {
        int sum = 0;
        for (int i = 0; i < digitPosition; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (digitPosition + 1 - i);
        }
        int digit = 11 - (sum % 11);
        return (digit >= 10) ? 0 : digit;
    }

    /**
     * Valida um CPF completo com dígito verificador.
     * Verifica:
     * - Exatamente 11 dígitos numéricos
     * - Rejeita CPFs com todos os dígitos iguais
     * - Valida ambos os dígitos verificadores (módulo 11)
     *
     * @param cpf String com o CPF (pode conter formatação; será limpo internamente)
     * @return true se o CPF é válido
     */
    public static boolean validateCpf(String cpf) {
        if (cpf == null) return false;
        cpf = cleanCpf(cpf);
        if (cpf.length() != 11) return false;
        if (allDigitsEqual(cpf)) return false;
        if (Character.getNumericValue(cpf.charAt(9))  != calculateVerifierDigit(cpf, 9))  return false;
        if (Character.getNumericValue(cpf.charAt(10)) != calculateVerifierDigit(cpf, 10)) return false;
        return true;
    }

    /**
     * Formata o CPF para exibição: 123.456.789-00
     */
    public String getFormattedCpf() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." +
                cpf.substring(3, 6) + "." +
                cpf.substring(6, 9) + "-" +
                cpf.substring(9);
    }

    // ========================
    // Getters e Setters
    // ========================

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCpf() {
        return cpf;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDate getRemovedAt() {
        return removedAt;
    }

    public String toString() {
        return "Nome: " + name + "\n" +
                "CPF: " + getFormattedCpf() + "\n" +
                "Contato: " + contact + "\n" +
                "Data de Nascimento: " + DateFormatter.format(birthDate) + "\n" +
                "Idade: " + calculateAge() + " anos" + "\n" +
                "Status: " + (active ? "Ativo" : "Inativo");
    }
}