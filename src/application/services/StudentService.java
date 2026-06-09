package application.services;

import application.OperationResult;
import domain.model.Student;
import exceptions.InvalidFormatFieldException;
import exceptions.RequiredFieldException;
import persistence.StudentRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import util.CollectionUtils;
import util.DateFormatter;

/**
 * Serviço responsável pelas operações específicas da entidade Student.
 *
 * Conhece apenas objetos do seu próprio domínio. Delega o armazenamento
 * e a persistência da coleção ao {@link StudentRepository}, composto como
 * atributo interno (relação de composição). O serviço cuida das regras de
 * negócio (validações de campo, formato do CPF, unicidade); o repositório
 * cuida da coleção e da persistência em arquivo.
 *
 * Política de comunicação de falhas:
 * - Campos obrigatórios vazios → {@link RequiredFieldException}.
 * - Entradas em formato inválido (CPF, data) → {@link InvalidFormatFieldException}.
 * - Resultados normais de consulta (não encontrado, lista vazia, CPF duplicado,
 *   data futura) seguem usando {@link OperationResult} com {@code success = false}.
 */
public class StudentService {

    private StudentRepository repository;

    public StudentService() {
        this.repository = new StudentRepository();
    }

    /**
     * Expõe o repositório composto.
     * Utilizado pelo orquestrador (FitManager) para coordenar persistência.
     */
    public StudentRepository getRepository() {
        return repository;
    }

    /**
     * Registra um novo aluno no sistema.
     * Valida campos obrigatórios, formato do CPF e unicidade.
     *
     * @return OperationResult com o Student criado em data (se sucesso)
     */
    public OperationResult<Student> registerStudent(
        String name,
        String cpf,
        String contact,
        String birthDateStr
    ) {
        name = validateRequiredFields(name, "Nome");
        cpf = validateRequiredFields(cpf, "CPF");
        contact = validateRequiredFields(contact, "Contato");
        birthDateStr = validateRequiredFields(birthDateStr, "Data de Nascimento");

        String cleanCpf = Student.cleanCpf(cpf);

        if (!Student.validateCpf(cleanCpf)) {
            throw new InvalidFormatFieldException("CPF", "11 dígitos numéricos com dígito verificador válido");
        }
        if (findActiveStudentByCpf(cleanCpf) != null) {
            return new OperationResult<>(false, "Já existe um aluno cadastrado com este CPF.");
        }

        LocalDate birthDate;
        try {
            birthDate = DateFormatter.parseDate(birthDateStr);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatFieldException("Data de Nascimento", DateFormatter.DATE_PATTERN + " (ex.: 30/07/1993)");
        }

        if (birthDate.isAfter(LocalDate.now())) {
            return new OperationResult<>(false, "A data de nascimento não pode ser uma data futura.");
        }

        Student student = new Student(name, cleanCpf, contact, birthDate);
        repository.add(student);

        return new OperationResult<>(true,
                "✅ Aluno " + student.getName() + " registrado com sucesso!", student);
    }

    /**
     * Valida se um campo obrigatório do aluno está vazio ou nulo.
     * Lança {@link RequiredFieldException} no primeiro campo vazio encontrado.
     * @return valor limpo, sem espaços desnecessários
     */
    private String validateRequiredFields(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new RequiredFieldException(fieldName);
        }

        return value.trim();
    }

    /**
     * Busca um aluno pelo CPF (apenas alunos ativos).
     *
     * @return OperationResult com o Student encontrado em data (se sucesso)
     */
    public OperationResult<Student> findByCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }

        String cleanCpf = Student.cleanCpf(cpf);

        if (!Student.validateCpf(cleanCpf)) {
            throw new InvalidFormatFieldException("CPF", "11 dígitos numéricos com dígito verificador válido");
        }

        Student student = findActiveStudentByCpf(cleanCpf);
        if (student != null) {
            return new OperationResult<>(true, "Aluno encontrado.", student);
        }

        return new OperationResult<>(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Remove (desativa) um aluno pelo CPF.
     * A verificação de matrículas ativas é responsabilidade do FitManager.
     *
     * @return OperationResult sem dado de retorno (operação de mutação)
     */
    public OperationResult<Void> removeStudent(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        String cleanCpf = Student.cleanCpf(cpf);

        Student student = findActiveStudentByCpf(cleanCpf);
        if (student != null) {
            student.deactivate();
            return new OperationResult<>(true,
                    "✅ Aluno " + student.getName() + " removido com sucesso.");
        }

        return new OperationResult<>(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Atualiza os dados de um aluno.
     * Apenas nome e contato podem ser alterados — CPF e data de nascimento são imutáveis.
     *
     * @return OperationResult com o Student atualizado em data (se sucesso)
     */
    public OperationResult<Student> updateStudent(
        String cpf,
        String newName,
        String newContact
    ) {
        if (cpf == null || cpf.isBlank()) {
            throw new RequiredFieldException("CPF");
        }
        String cleanCpf = Student.cleanCpf(cpf);

        Student student = findActiveStudentByCpf(cleanCpf);
        if (student != null) {
            if (newName != null && !newName.isBlank()) {
                student.setName(newName.trim());
            }
            if (newContact != null && !newContact.isBlank()) {
                student.setContact(newContact.trim());
            }
            return new OperationResult<>(true,
                    "✅ Cadastro do aluno atualizado com sucesso!", student);
        }

        return new OperationResult<>(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Lista todos os alunos ativos.
     *
     * @return OperationResult com ArrayList&lt;Student&gt; em data
     */
    public OperationResult<ArrayList<Student>> listAll() {
        ArrayList<Student> activeStudents = CollectionUtils.filter(
                repository.listAll(),
                Student::isActive
        );

        if (activeStudents.isEmpty()) {
            return new OperationResult<>(false, "Nenhum aluno cadastrado no sistema.");
        }

        return new OperationResult<>(true,
                activeStudents.size() + " aluno(s) encontrado(s).", activeStudents);
    }

    /**
     * Encontra e retorna um aluno ativo, se existir, com o CPF informado.
     */
    public Student findActiveStudentByCpf(String cpf) {
        for (Student student : repository.listAll()) {
            if (student.getCpf().equals(cpf) && student.isActive()) {
                return student;
            }
        }
        return null;
    }
}
