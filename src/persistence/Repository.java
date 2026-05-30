package persistence;

import exceptions.PersistenceException;

import java.util.ArrayList;

/**
 * Classe genérica abstrata que centraliza o comportamento comum aos
 * repositórios concretos do sistema — manter uma coleção tipada de objetos,
 * expor operações estruturais (listagem, contagem, adição) e declarar a
 * obrigação de persistência por meio de dois métodos abstratos.
 *
 * Parametrizada com {@code <T>}, suporta qualquer tipo de entidade:
 * - {@code StudentRepository extends Repository<Student>}
 * - {@code PlanRepository extends Repository<Plan>}
 * - {@code EnrollmentRepository extends Repository<Enrollment>}
 *
 * Relação com os serviços: COMPOSIÇÃO. Cada serviço mantém um repositório
 * concreto como atributo interno e delega a ele a responsabilidade de
 * armazenamento e (futuramente) persistência em arquivo. Isso preserva o
 * princípio de responsabilidade única — o serviço cuida das regras de
 * negócio do seu domínio; o repositório cuida da coleção e da persistência.
 *
 * Os métodos {@link #save()} e {@link #load()} são declarados como abstratos
 * e lançam {@link PersistenceException} (verificada), forçando que cada
 * subclasse forneça sua própria implementação de leitura e escrita e que
 * quem chamar trate explicitamente eventuais falhas de arquivo. A
 * implementação concreta desses métodos é assunto futuro.
 *
 * @param <T> tipo da entidade armazenada pelo repositório
 */
public abstract class Repository<T> {

    /**
     * Diretório base onde os arquivos de persistência são gravados/lidos.
     * Compartilhado por todos os repositórios concretos para que os arquivos
     * fiquem agrupados num único local relativo à pasta de execução do programa.
     */
    public static final String DATA_DIR = "data";

    /**
     * Coleção interna de itens do repositório. Visível para as subclasses
     * concretas que implementam {@link #save()} e {@link #load()}.
     */
    protected ArrayList<T> items;

    protected Repository() {
        this.items = new ArrayList<>();
    }

    /**
     * Retorna uma cópia da coleção interna.
     * Permite que os serviços iterem sobre os itens sem expor a coleção
     * original a modificações externas.
     */
    public ArrayList<T> listAll() {
        return new ArrayList<>(items);
    }

    /**
     * Retorna a quantidade de itens armazenados no repositório.
     */
    public int count() {
        return items.size();
    }

    /**
     * Indica se o repositório está vazio.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Adiciona um item ao repositório.
     */
    public void add(T item) {
        items.add(item);
    }

    /**
     * Remove um item do repositório.
     * @return {@code true} se o item estava presente e foi removido
     */
    public boolean remove(T item) {
        return items.remove(item);
    }

    /**
     * Persiste o conteúdo do repositório em meio externo (arquivo).
     * A implementação concreta é responsabilidade de cada subclasse.
     *
     * @throws PersistenceException se ocorrer falha de escrita
     */
    public abstract void save() throws PersistenceException;

    /**
     * Carrega o conteúdo do repositório a partir de meio externo (arquivo).
     * A implementação concreta é responsabilidade de cada subclasse.
     *
     * @throws PersistenceException se ocorrer falha de leitura ou se o
     *         conteúdo do arquivo estiver corrompido
     */
    public abstract void load() throws PersistenceException;
}
