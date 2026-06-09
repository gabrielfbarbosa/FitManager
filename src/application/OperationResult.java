package application;

/**
 * Classe de retorno padronizado para todas as operações do sistema.
 *
 * Em vez de retornar booleanos ou lançar exceções para resultados normais,
 * os métodos retornam um {@code OperationResult} contendo: sucesso, mensagem
 * descritiva e dado opcional tipado.
 *
 * Parametrizada com generics: o tipo do dado retornado é declarado na assinatura
 * de cada método (ex.: {@code OperationResult<Student>},
 * {@code OperationResult<ArrayList<Plan>>}). O compilador verifica a consistência
 * de tipos em todos os pontos de uso, e os casts explícitos sobre {@link #getData()}
 * deixam de ser necessários.
 *
 * Operações que comunicam apenas sucesso/falha (sem dado de retorno) usam
 * {@code OperationResult<Void>} com {@code data = null} — convenção uniforme
 * em todo o sistema.
 *
 * Os dois construtores continuam disponíveis por sobrecarga:
 * - Simples: sucesso + mensagem (data = null)
 * - Completo: sucesso + mensagem + dado tipado
 *
 * @param <T> tipo do dado retornado em caso de sucesso (ou {@link Void} se não houver)
 */
public class OperationResult<T> {

    private boolean success;
    private String message;
    private T data;

    /**
     * Construtor simples — apenas sucesso e mensagem. {@code data} fica {@code null}.
     */
    public OperationResult(boolean success, String message) {
        this(success, message, null);
    }

    /**
     * Construtor completo — sucesso, mensagem e dado de retorno tipado.
     */
    public OperationResult(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Retorna o dado de tipo {@code T} associado à operação.
     * O compilador garante que o tipo retornado é o declarado na assinatura
     * do método chamador, sem necessidade de cast.
     */
    public T getData() {
        return data;
    }
}
