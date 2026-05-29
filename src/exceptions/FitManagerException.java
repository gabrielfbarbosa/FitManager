package exceptions;

/**
 * Exceção-base de todo o sistema FitManager.
 *
 * Serve como raiz das exceções de domínio do sistema — validação e regras de
 * negócio — permitindo capturar qualquer falha dessas categorias com um único
 * bloco {@code catch (FitManagerException e)} como última linha de defesa,
 * garantindo que nenhuma falha de domínio chegue ao terminal como stack trace.
 *
 * Estende {@link RuntimeException} (não verificada): as exceções de validação
 * e de regra de negócio que dela derivam não obrigam a declaração de
 * {@code throws} pelas assinaturas, pois representam situações recuperáveis
 * que são tratadas de forma centralizada nos menus, normalmente capturando
 * a categoria correspondente.
 *
 * As falhas de persistência seguem uma hierarquia VERIFICADA separada
 * ({@link PersistenceException}), por exigirem tratamento explícito pelo
 * compilador — a justificativa dessa separação está documentada no relatório.
 */
public class FitManagerException extends RuntimeException {

    public FitManagerException(String message) {
        super(message);
    }

    public FitManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
