package exceptions;

/**
 * Lançada quando se tenta matricular um aluno que já possui uma matrícula
 * ativa. O sistema permite apenas uma matrícula ativa por aluno — a atual
 * deve ser cancelada antes de uma nova ser realizada.
 */
public class DuplicatedEnrollmentException extends BusinessException {

    private final String cpf;

    public DuplicatedEnrollmentException(String cpf) {
        super("O aluno (CPF " + cpf + ") já possui uma matrícula ativa. "
                + "Cancele a matrícula atual antes de realizar uma nova.");
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }
}
