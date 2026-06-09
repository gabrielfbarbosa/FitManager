package exceptions;

/**
 * Lançada quando um campo obrigatório está vazio ou nulo.
 *
 * Guarda o nome do campo ausente, permitindo que a mensagem identifique
 * exatamente qual dado faltou — em vez de uma mensagem genérica de erro.
 */
public class RequiredFieldException extends ValidationException {

    private final String fieldName;

    public RequiredFieldException(String fieldName) {
        super("O campo \"" + fieldName + "\" é obrigatório.");
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
