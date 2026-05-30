package persistence;

import domain.model.Student;
import exceptions.CorruptedFileException;
import exceptions.PersistenceException;
import exceptions.WriteFailureException;
import util.DateFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Repositório concreto para alunos.
 *
 * Herda de {@link Repository} todo o comportamento estrutural — coleção
 * interna tipada como {@code ArrayList<Student>}, listagem, contagem,
 * adição e remoção — e implementa as operações de persistência
 * específicas do tipo {@link Student} em arquivo texto (CSV com {@code ;}
 * como delimitador).
 *
 * Usado por composição em {@code StudentService}.
 *
 * Arquivo: {@code data/students.txt}.
 * Formato por linha: {@code cpf;name;contact;birthDate;active;removedAt}.
 * Linhas iniciadas por {@code #} são tratadas como comentário.
 * Valores com {@code ;} interno são escapados como {@code ,} na escrita,
 * preservando a integridade do delimitador.
 */
public class StudentRepository extends Repository<Student> {

    private static final String FILE_NAME = "students.txt";
    private static final String HEADER = "# FitManager — Alunos\n# cpf;name;contact;birthDate;active;removedAt";

    /**
     * Persiste a coleção de alunos em {@code data/students.txt}.
     * Usa try-with-resources para garantir o fechamento do recurso mesmo
     * em caso de falha no meio da escrita.
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
            for (Student student : items) {
                writer.write(serialize(student));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new WriteFailureException(path.toString(), e);
        }
    }

    /**
     * Recupera a coleção de alunos a partir de {@code data/students.txt}.
     * Ausência do arquivo é tratada como situação normal (primeira execução):
     * o repositório inicia vazio sem lançar exceção.
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
     * Serializa um aluno em uma linha CSV.
     */
    private String serialize(Student student) {
        return student.getCpf() + ";"
                + escape(student.getName()) + ";"
                + escape(student.getContact()) + ";"
                + DateFormatter.formatDate(student.getBirthDate()) + ";"
                + student.isActive() + ";"
                + (student.getRemovedAt() == null ? "" : DateFormatter.formatDate(student.getRemovedAt()));
    }

    /**
     * Desserializa uma linha CSV em {@link Student}.
     * Lança {@link CorruptedFileException} se a linha estiver mal formada.
     */
    private Student deserialize(String line, int lineNumber) throws CorruptedFileException {
        String[] parts = line.split(";", -1);
        if (parts.length < 5) {
            throw new CorruptedFileException(filePath().toString(),
                    "linha " + lineNumber + " com número insuficiente de campos");
        }
        try {
            String cpf = parts[0];
            String name = parts[1];
            String contact = parts[2];
            LocalDate birthDate = DateFormatter.parseDate(parts[3]);
            boolean active = Boolean.parseBoolean(parts[4]);
            LocalDate removedAt = (parts.length > 5 && !parts[5].isEmpty())
                    ? DateFormatter.parseDate(parts[5]) : null;
            return new Student(name, cpf, contact, birthDate, active, removedAt);
        } catch (DateTimeParseException e) {
            throw new CorruptedFileException(filePath().toString(),
                    "data inválida na linha " + lineNumber, e);
        }
    }

    /**
     * Substitui {@code ;} por {@code ,} dentro de campos texto, evitando
     * que o delimitador apareça no conteúdo e quebre a leitura.
     */
    private String escape(String value) {
        return value == null ? "" : value.replace(";", ",");
    }

    private Path filePath() {
        return Paths.get(DATA_DIR, FILE_NAME);
    }

    /**
     * Busca um aluno ativo pelo CPF.
     * @return o Student ativo com o CPF informado, ou null se não encontrado.
     */
    public Student findByCpf(String cpf) {
        for (Student s : items) {
            if (s.getCpf().equals(cpf) && s.isActive()) {
                return s;
            }
        }
        return null;
    }
}
