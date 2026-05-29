package exceptions;

/**
 * Categoria de exceções para entradas mal formadas ou campos obrigatórios
 * ausentes — falhas detectadas ao validar os dados fornecidos pelo usuário.
 *
 * Não verificada (herda de {@link FitManagerException}). Permite que um menu
 * capture qualquer falha de validação com um único bloco
 * {@code catch (ValidationException e)}, sem precisar listar cada subtipo.
 *
 * Subtipos: {@link RequiredFieldException}, {@link InvalidFormatFieldException}.
 */
public class ValidationException extends FitManagerException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
