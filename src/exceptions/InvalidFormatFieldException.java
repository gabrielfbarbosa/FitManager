package exceptions;

/**
 * Lançada quando o valor de um campo não está no formato esperado —
 * por exemplo, um CPF com dígitos inválidos, uma data fora do padrão
 * dd/MM/yyyy ou um texto onde se esperava um número.
 *
 * Guarda o nome do campo e o formato esperado, permitindo que a mensagem
 * informe ao usuário não apenas que a entrada foi rejeitada, mas o que
 * era esperado — orientando a correção sem reiniciar a operação.
 */
public class InvalidFormatFieldException extends ValidationException {

    private final String fieldName;
    private final String expectedFormat;

    public InvalidFormatFieldException(String fieldName, String expectedFormat) {
        super("O campo \"" + fieldName + "\" está em formato inválido. "
                + "Formato esperado: " + expectedFormat + ".");
        this.fieldName = fieldName;
        this.expectedFormat = expectedFormat;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getExpectedFormat() {
        return expectedFormat;
    }
}
