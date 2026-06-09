package util;

import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Utilitário responsável por operações genéricas sobre coleções.
 */
public final class CollectionUtils {

    private CollectionUtils() {
        // Classe utilitária — não deve ser instanciada.
    }

    /**
     * Filtra uma lista usando um critério recebido por parâmetro.
     *
     * @param source lista original
     * @param criterion critério que define quais itens entram no resultado
     * @param <T> tipo dos elementos da lista
     * @return nova lista contendo apenas os itens aceitos pelo critério
     */
    public static <T> ArrayList<T> filter(ArrayList<T> source, Predicate<T> criterion) {
        ArrayList<T> result = new ArrayList<>();
        for (T item : source) {
            if (criterion.test(item)) {
                result.add(item);
            }
        }
        return result;
    }
}
