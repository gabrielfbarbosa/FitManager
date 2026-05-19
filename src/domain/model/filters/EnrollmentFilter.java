package domain.model.filters;

import domain.model.Enrollment;

/**
 * Interface funcional que define um critério de filtro para matrículas.
 *
 * Permite criar filtros polimórficos: cada implementação encapsula
 * uma regra de negócio diferente para selecionar matrículas.
 *
 * Uso: o EnrollmentService recebe um EnrollmentFilter e aplica o método
 * matches() a cada matrícula da coleção, retornando apenas as que atendem
 * ao critério. Isso elimina a duplicação de loops de filtragem e permite
 * adicionar novos filtros sem alterar o serviço.
 *
 * Demonstra o princípio Open/Closed (OCP): o sistema está aberto para
 * extensão (novos filtros) e fechado para modificação (EnrollmentService
 * não precisa ser alterado para suportar novos critérios).
 */
public interface EnrollmentFilter {

    /**
     * Verifica se uma matrícula atende ao critério deste filtro.
     *
     * @param enrollment matrícula a ser avaliada
     * @return true se a matrícula atende ao critério
     */
    public boolean matches(Enrollment enrollment);

    /**
     * Retorna uma descrição legível do filtro para exibição em relatórios.
     * Exemplo: "Matrículas Ativas", "Saldo Pendente", "Plano: Trimestral"
     *
     * @return descrição do filtro
     */
    public String getDescription();
}
