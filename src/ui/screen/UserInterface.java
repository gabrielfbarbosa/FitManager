package ui.screen;

import java.time.LocalDate;

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
 *
 * Implementação compartilhada — a fim de evitar duplicação dos loops de
 * validação entre as duas UIs concretas, toda a lógica comum
 * (showMenu, getInput obrigatório, getInt, getDouble, getDate, getCpf,
 * getPlan, selectPaymentType, collectPaymentData) reside na classe
 * abstrata {@link BaseUserInterface}. As UIs concretas estendem
 * essa abstrata e fornecem apenas as primitivas de I/O.
 */
public interface UserInterface {

    /**
     * Exibe um menu com título e opções, repetindo até receber uma escolha
     * válida no intervalo {@code 1..maxOption}. Entradas não numéricas ou
     * fora do intervalo geram {@code showError} e o menu é reexibido.
     *
     * @param title     título do menu (sufixo exibido após "FitManager")
     * @param options   texto completo com as opções numeradas
     * @param maxOption número da última opção do menu (intervalo válido: 1..maxOption)
     * @return o número da opção escolhida, ou {@code null} se cancelado
     */
    public Integer showMenu(String title, String options, int maxOption);

    /**
     * Captura uma entrada de texto do usuário (campo opcional).
     * Retorna a string digitada (possivelmente vazia) ou {@code null} se
     * o usuário cancelar o diálogo. Usar para campos em que o vazio tem
     * significado próprio (ex.: edição em que "deixar em branco mantém
     * o valor atual").
     *
     * @param prompt texto do prompt exibido
     * @return a string digitada pelo usuário, ou null se cancelou
     */
    public String getInput(String prompt);

    /**
     * Captura uma entrada de texto obrigatória.
     * Repete o diálogo enquanto a entrada for nula ou em branco, exibindo
     * {@code O campo "<fieldName>" é obrigatório.} via {@code showError}.
     * Retorna {@code null} apenas quando o usuário cancela.
     *
     * @param prompt    texto do prompt exibido
     * @param fieldName nome legível do campo (usado na mensagem de erro)
     * @return a string não vazia digitada, ou {@code null} se cancelado
     */
    public String getInput(String prompt, String fieldName);

    /**
     * Captura uma entrada inteira do usuário, repetindo até receber um
     * valor parseável. Cancelamento retorna {@code null}.
     */
    public Integer getInt(String prompt, String fieldName);

    /**
     * Captura uma entrada decimal do usuário, repetindo até receber um
     * valor parseável. Aceita vírgula como separador decimal (padrão pt-BR).
     * Cancelamento retorna {@code null}.
     */
    public Double getDouble(String prompt, String fieldName);

    /**
     * Captura uma data do usuário no padrão {@code dd/MM/yyyy}, repetindo
     * até receber uma data válida. Cancelamento retorna {@code null}.
     */
    public LocalDate getDate(String prompt, String fieldName);

    /**
     * Exibe uma mensagem de sucesso/informação.
     */
    public void showMessage(String message);

    /**
     * Exibe uma mensagem de erro.
     */
    public void showError(String message);

    /**
     * Exibe uma mensagem longa com suporte a rolagem.
     */
    public void showScrollableMessage(String message);
}
