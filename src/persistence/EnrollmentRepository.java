package persistence;

import domain.model.Enrollment;
import exceptions.PersistenceException;

/**
 * Repositório concreto para matrículas.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Enrollment>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Enrollment}.
 *
 * Particularidades desta entidade tratadas pelo repositório:
 *  - A coleção referencia objetos {@code Plan} (heterogêneo) e pagamentos
 *    {@code Payment} (também heterogêneo). Ambos os tipos polimórficos
 *    devem ter o tipo concreto preservado na persistência (implementação futura).
 *  - O contador estático {@code Enrollment.nextCode}, responsável por
 *    gerar códigos sequenciais únicos por matrícula, precisa ser
 *    persistido e restaurado para que não haja repetição de códigos
 *    entre sessões. Essa responsabilidade reside aqui, no repositório
 *    de matrículas, e não na superclasse genérica.
 *
 * Usado por composição em {@code EnrollmentService}.
 *
 * As implementações de {@link #save()} e {@link #load()} são preenchidas
 * futuramente (persistência em arquivos texto). Por enquanto, são no-op.
 */
public class EnrollmentRepository extends Repository<Enrollment> {

    /**
     * Persiste a coleção de matrículas em arquivo, incluindo o estado do
     * contador {@code nextCode} e preservando os tipos concretos das
     * subclasses de {@code Plan} e {@code Payment} referenciadas.
     * Implementação concreta futura.
     *
     * @throws PersistenceException se ocorrer falha de escrita
     */
    @Override
    public void save() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }

    /**
     * Recupera a coleção de matrículas a partir do arquivo, restaurando
     * o contador {@code nextCode} e reconstituindo as referências para
     * alunos, planos e pagamentos com seus tipos concretos.
     * Implementação concreta futura.
     *
     * @throws PersistenceException se ocorrer falha de leitura ou
     *         se o arquivo estiver corrompido
     */
    @Override
    public void load() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }
}
