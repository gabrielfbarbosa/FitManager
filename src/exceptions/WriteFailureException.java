package exceptions;

/**
 * Lançada quando ocorre uma falha ao gravar dados em arquivo — disco cheio,
 * arquivo sem permissão de escrita ou erro de I/O no meio da gravação.
 *
 * É a falha mais crítica do encerramento, pois os dados em memória estão
 * prestes a ser descartados. Guarda o nome do arquivo afetado para que o
 * usuário seja informado de exatamente o que não pôde ser salvo, em vez de
 * o sistema encerrar silenciosamente.
 *
 * Tipicamente encapsula uma {@link java.io.IOException} de origem, ocultando
 * o detalhe de infraestrutura das camadas superiores.
 */
public class WriteFailureException extends PersistenceException {

    private final String fileName;

    public WriteFailureException(String fileName, Throwable cause) {
        super("Falha ao gravar o arquivo \"" + fileName + "\". Os dados podem não ter sido salvos.", cause);
        this.fileName = fileName;
    }

    public WriteFailureException(String fileName, String detail) {
        super("Falha ao gravar o arquivo \"" + fileName + "\": " + detail);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
