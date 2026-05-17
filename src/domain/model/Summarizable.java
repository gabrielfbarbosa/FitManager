package domain.model;

/**
 * Interface que define a capacidade de gerar um resumo textual curto.
 *
 * Implementada pelas entidades de domínio (Student, Plan, Enrollment, Payment)
 * para fornecer uma representação resumida em uma única linha,
 * diferente do toString() que retorna os dados completos.
 *
 * Uso típico: listagens compactas, logs e relatórios resumidos
 * onde não se deseja exibir todos os detalhes da entidade.
 *
 * Demonstra o uso de interface como contrato polimórfico:
 * qualquer classe que implemente Summarizable pode ser tratada
 * de forma uniforme em contextos que precisam apenas de um resumo.
 */
public interface Summarizable {

    /**
     * Retorna um resumo curto da entidade em uma única linha.
     *
     * Exemplos:
     *  - Student:    "João Silva | CPF: 123.456.789-00 | 25 anos"
     *  - Plan:       "Plano Gold | Trimestral | R$ 89,90/mês"
     *  - Enrollment: "Matrícula Código: 5 | Plano Gold | Ativa | Saldo: R$ 150,00"
     *  - Payment:    "Pagamento Código: 3 | R$ 200,00 | PIX | 14/04/2026"
     *
     * @return String com o resumo da entidade
     */
    public String getSummary();
}
