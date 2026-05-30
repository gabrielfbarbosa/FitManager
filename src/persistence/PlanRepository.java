package persistence;

import domain.model.enums.PlanType;
import domain.model.plans.Plan;
import domain.model.plans.PlanFactory;
import exceptions.CorruptedFileException;
import exceptions.PersistenceException;
import exceptions.WriteFailureException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Repositório concreto para planos.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Plan>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Plan} em arquivo texto (CSV com {@code ;}
 * como delimitador).
 *
 * A coleção é heterogênea: armazena instâncias das subclasses concretas
 * ({@code MonthlyPlan}, {@code QuarterlyPlan}, {@code SemiAnnualPlan},
 * {@code AnnualPlan}). O campo {@code type} é o primeiro de cada linha
 * e é usado por {@link PlanFactory} para instanciar a subclasse correta
 * ao carregar, preservando o polimorfismo após a reconstrução.
 *
 * Usado por composição em {@code PlanService}.
 *
 * Arquivo: {@code data/plans.txt}.
 * Formato por linha: {@code type;name;description;minimumDuration;pricePerMonth}.
 */
public class PlanRepository extends Repository<Plan> {

    private static final String FILE_NAME = "plans.txt";
    private static final String HEADER = "# FitManager — Planos\n# type;name;description;minimumDuration;pricePerMonth";

    /**
     * Persiste a coleção de planos em {@code data/plans.txt}, preservando
     * o tipo concreto de cada subclasse via o campo {@code type}.
     *
     * @throws PersistenceException se ocorrer falha de escrita
     */
    @Override
    public void save() throws PersistenceException {
        Path path = filePath();
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write(HEADER);
            writer.newLine();
            for (Plan plan : items) {
                writer.write(serialize(plan));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
    }

    /**
     * Recupera a coleção de planos a partir de {@code data/plans.txt},
     * reconstituindo cada elemento com a subclasse concreta correta via
     * {@link PlanFactory}.
     *
     * @throws PersistenceException se o arquivo estiver corrompido ou
     *         se ocorrer falha de leitura
     */
    @Override
    public void load() throws PersistenceException {
        Path path = filePath();
        if (!Files.exists(path)) {
            return; // primeira execução — arquivo ainda não existe
        }
        items.clear();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                items.add(deserialize(trimmed, lineNumber));
            }
        } catch (IOException e) {
            throw new CorruptedFileException(path.toString(), "erro de leitura", e);
        }
    }

    /**
     * Serializa um plano em uma linha CSV.
     * O campo {@code type} (primeira posição) identifica a subclasse concreta.
     */
    private String serialize(Plan plan) {
        return plan.getType().name() + ";"
                + escape(plan.getName()) + ";"
                + escape(plan.getDescription()) + ";"
                + plan.getMinimumDuration() + ";"
                + plan.getPricePerMonth();
    }

    /**
     * Desserializa uma linha CSV em {@link Plan} via {@link PlanFactory},
     * preservando o tipo concreto.
     */
    private Plan deserialize(String line, int lineNumber) throws CorruptedFileException {
        String[] parts = line.split(";", -1);
        if (parts.length < 5) {
            throw new CorruptedFileException(filePath().toString(),
                    "linha " + lineNumber + " com número insuficiente de campos");
        }
        try {
            PlanType type = PlanType.valueOf(parts[0]);
            String name = parts[1];
            String description = parts[2];
            int minimumDuration = Integer.parseInt(parts[3]);
            double pricePerMonth = Double.parseDouble(parts[4]);
            return PlanFactory.create(type, name, description, minimumDuration, pricePerMonth);
        } catch (IllegalArgumentException e) {
            throw new CorruptedFileException(filePath().toString(),
                    "valor inválido na linha " + lineNumber, e);
        }
    }

    private String escape(String value) {
        return value == null ? "" : value.replace(";", ",");
    }

    private Path filePath() {
        return Paths.get(DATA_DIR, FILE_NAME);
    }
}
