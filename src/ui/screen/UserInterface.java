package ui.screen;

/**
 * Interface que define o contrato de entrada e saída do sistema.
 *
 * Qualquer implementação concreta deve fornecer meios para exibir menus,
 * capturar entradas de texto, mostrar mensagens informativas e erros.
 *
 * Implementações disponíveis:
 * - JOptionPaneUI: interface gráfica com diálogos Swing
 * - TerminalUI: interface de linha de comando via terminal
 *
 * Os menus e serviços recebem esta interface por parâmetro,
 * permitindo trocar a implementação sem alterar nenhuma outra classe.
 */
public interface UserInterface {

    /**
     * Exibe um menu com título e opções, retornando a opção escolhida pelo usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param title   título do menu
     * @param options texto completo com as opções numeradas
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String showMenu(String title, String options);

    /**
     * Captura uma entrada de texto do usuário.
     * Retorna null se o usuário cancelar o diálogo.
     *
     * @param prompt texto do prompt exibido
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String getInput(String prompt);

    /**
     * Exibe uma mensagem de sucesso/informação.
     *
     * @param message texto da mensagem
     */
    public void showMessage(String message);

    /**
     * Exibe uma mensagem de erro.
     *
     * @param message texto do erro
     */
    public void showError(String message);

    /**
     * Exibe uma mensagem longa com suporte a rolagem.
     *
     * @param message texto da informação exibida
     */
    public void showScrollableMessage(String message);
}
