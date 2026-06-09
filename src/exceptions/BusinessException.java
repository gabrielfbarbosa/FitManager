package exceptions;

/**
 * Categoria de exceções para violações de regras de negócio do domínio —
 * operações que o usuário solicitou mas que ferem uma restrição do sistema.
 *
 * Não verificada (herda de {@link FitManagerException}). Seguindo o critério
 * de Joshua Bloch (Effective Java), as exceções de negócio aqui são
 * recuperáveis — o menu captura, exibe a mensagem e permite nova tentativa —
 * mas não se exige que o compilador force o tratamento em cada ponto da
 * cadeia, já que os menus já verificam o resultado das operações.
 *
 * Subtipos: {@link StudentWithActiveEnrollmentException},
 * {@link DuplicatedEnrollmentException}, {@link DuplicatedPlanException}.
 */
public class BusinessException extends FitManagerException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
