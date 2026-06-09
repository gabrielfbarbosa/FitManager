package exceptions;

/**
 * Lançada ao tentar cadastrar um plano com um nome que já existe no sistema.
 * O nome do plano é único — serve como identificador nas consultas e
 * referências de matrícula.
 */
public class DuplicatedPlanException extends BusinessException {

    private final String planName;

    public DuplicatedPlanException(String planName) {
        super("Já existe um plano cadastrado com o nome \"" + planName + "\".");
        this.planName = planName;
    }

    public String getPlanName() {
        return planName;
    }
}
