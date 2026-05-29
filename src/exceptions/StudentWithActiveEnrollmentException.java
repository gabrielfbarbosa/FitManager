package exceptions;

/**
 * Lançada ao tentar remover um aluno que ainda possui matrícula ativa.
 *
 * A regra de negócio impede a remoção para preservar a integridade do
 * histórico: a matrícula ativa deve ser cancelada antes que o aluno
 * possa ser desativado.
 */
public class StudentWithActiveEnrollmentException extends BusinessException {

    private final String cpf;

    public StudentWithActiveEnrollmentException(String cpf) {
        super("Não é possível remover o aluno (CPF " + cpf + "): ele possui matrícula ativa. "
                + "Cancele a matrícula antes de remover o aluno.");
        this.cpf = cpf;
    }

    public String getCpf() {
        return cpf;
    }
}
