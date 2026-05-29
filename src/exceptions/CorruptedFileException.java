package exceptions;

/**
 * Lançada quando um arquivo de dados existe mas não pode ser interpretado —
 * linha com número incorreto de campos, valor que não converte para o tipo
 * esperado, tipo de subclasse desconhecido ou referência a um identificador
 * inexistente nas coleções já carregadas.
 *
 * Diferente de um arquivo ausente (situação normal na primeira execução),
 * um arquivo corrompido é um problema real que exige decisão controlada
 * sobre como prosseguir. Guarda o nome do arquivo afetado.
 */
public class CorruptedFileException extends PersistenceException {

    private final String fileName;

    public CorruptedFileException(String fileName, String detail) {
        super("O arquivo \"" + fileName + "\" está corrompido ou em formato inesperado: " + detail);
        this.fileName = fileName;
    }

    public CorruptedFileException(String fileName, String detail, Throwable cause) {
        super("O arquivo \"" + fileName + "\" está corrompido ou em formato inesperado: " + detail, cause);
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
