package application;

/**
        * Classe de retorno padronizado para todas as operações do sistema.
 *
         * Em vez de retornar booleanos ou lançar exceções, os métodos retornam
 * um OperationResult contendo: sucesso, mensagem descritiva e dado opcional.
 *
         * Oferece dois construtores por sobrecarga:
        * - Simples: sucesso + mensagem
 * - Completo: sucesso + mensagem + dado de retorno
 *
         * O campo data (Object) permite que operações retornem objetos junto ao resultado.
        * Em etapas futuras, Object será substituído por um tipo genérico T.
        */
public class OperationResult {

    private boolean success;
    private String message;
    private Object data;

    /**
     * Construtor simples — apenas sucesso e mensagem.
     */
    public OperationResult(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.data = null;
    }

    /**
     * Construtor completo — sucesso, mensagem e dado de retorno.
     */
    public OperationResult(boolean success, String message, Object data) {
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

    public Object getData() {
        return data;
    }
}
