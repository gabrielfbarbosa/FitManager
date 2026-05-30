package persistence;

import domain.model.Student;
import exceptions.PersistenceException;

/**
 * Repositório concreto para alunos.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Student>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Student}.
 *
 * Usado por composição em {@code StudentService}.
 *
 * As implementações de {@link #save()} e {@link #load()} são preenchidas
 * futuramente (persistência em arquivos texto). Por enquanto, são no-op
 * para manter o sistema funcional em memória.
 */
public class StudentRepository extends Repository<Student> {

    /**
     * Persiste a coleção de alunos em arquivo.
     * Implementação concreta futura.
     *
     * @throws PersistenceException se ocorrer falha de escrita
     */
    @Override
    public void save() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }

    /**
     * Recupera a coleção de alunos a partir do arquivo.
     * Implementação concreta na futura.
     *
     * @throws PersistenceException se ocorrer falha de leitura ou
     *         se o arquivo estiver corrompido
     */
    @Override
    public void load() throws PersistenceException {
        // Implementação futura — persistência em arquivos texto
    }
}
