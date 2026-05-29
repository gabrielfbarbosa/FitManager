package exceptions;

/**
 * Categoria de exceções para falhas de leitura e escrita de arquivos.
 *
 * VERIFICADA: estende diretamente {@link Exception} (e não
 * {@link FitManagerException}, que é não verificada). Essa escolha é
 * intencional — o compilador obriga os repositórios e o FitManager a
 * tratarem explicitamente as falhas de persistência via {@code throws} ou
 * {@code try-catch}. Diferentemente das falhas de domínio, uma falha de
 * arquivo é uma situação que o chamador precisa conhecer em tempo de
 * compilação para decidir o que fazer (iniciar com dados vazios, encerrar
 * ou avisar o usuário antes de descartar os dados em memória).
 *
 * Como consequência dessa separação, {@code catch (FitManagerException e)}
 * NÃO captura falhas de persistência — elas são tratadas no ponto de
 * inicialização/encerramento, respeitando a fronteira entre camadas.
 *
 * Subtipos: {@link CorruptedFileException}, {@link WriteFailureException}.
 */
public class PersistenceException extends Exception {

    public PersistenceException(String message) {
        super(message);
    }

    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
