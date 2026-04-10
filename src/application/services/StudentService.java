package application.services;

import application.OperationResult;
import domain.model.Student;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Serviço responsável por manter a coleção de alunos em memória
 * e implementar as operações específicas da entidade Student.
 *
 * Conhece apenas objetos do seu próprio domínio.
 */
public class StudentService {

    private ArrayList<Student> students;

    public StudentService() {
        this.students = new ArrayList<>();
    }

    /**
     * Registra um novo aluno no sistema.
     * Valida campos obrigatórios, formato do CPF e unicidade.
     *
     * @return OperationResult com o Student criado em data (se sucesso)
     */
    public OperationResult registerStudent(
        String name,
        String cpf,
        String contact,
        String birthDateStr
    ) {
        OperationResult fieldCheck = validateRequiredFields(name, cpf, contact, birthDateStr);
        if (!fieldCheck.isSuccess()) {
            return fieldCheck;
        }

        String cleanCpf = Student.cleanCpf(cpf);

        if (!Student.validateCpf(cleanCpf)) {
            return new OperationResult(false, "CPF inválido. Verifique os dígitos e tente novamente.");
        }
        if (cpfExists(cleanCpf)) {
            return new OperationResult(false, "Já existe um aluno cadastrado com este CPF.");
        }

        LocalDate birthDate = LocalDate.parse(
                birthDateStr.trim(),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        if (birthDate.isAfter(LocalDate.now())) {
            return new OperationResult(false, "A data de nascimento não pode ser uma data futura.");
        }

        Student student = new Student(name.trim(), cleanCpf, contact.trim(), birthDate);
        students.add(student);

        return new OperationResult(true,
                "✅ Aluno " + student.getName() + " registrado com sucesso!", student);
    }

    /**
     * Valida que nenhum campo obrigatório do aluno está vazio.
     */
    private OperationResult validateRequiredFields(
        String name,
        String cpf,
        String contact,
        String birthDateStr
    ) {
        if (name == null || name.trim().isEmpty()) {
            return new OperationResult(false, "O nome é obrigatório.");
        }
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório.");
        }
        if (contact == null || contact.trim().isEmpty()) {
            return new OperationResult(false, "O contato é obrigatório.");
        }
        if (birthDateStr == null || birthDateStr.trim().isEmpty()) {
            return new OperationResult(false, "A data de nascimento é obrigatória.");
        }
        return new OperationResult(true, "ok");
    }

    /**
     * Busca um aluno pelo CPF (apenas alunos ativos).
     *
     * @return OperationResult com o Student encontrado em data (se sucesso)
     */
    public OperationResult findByCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return new OperationResult(false, "O CPF é obrigatório para consulta.");
        }

        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                return new OperationResult(true, "Aluno encontrado.", student);
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Remove (desativa) um aluno pelo CPF.
     * A verificação de matrículas ativas é responsabilidade do FitManager.
     *
     * @return OperationResult indicando sucesso ou falha
     */
    public OperationResult removeStudent(String cpf) {
        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                student.deactivate();
                return new OperationResult(true,
                        "✅ Aluno " + student.getName() + " removido com sucesso.");
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Atualiza os dados de um aluno.
     * Apenas nome e contato podem ser alterados — CPF e data de nascimento são imutáveis.
     *
     * @return OperationResult com o Student atualizado em data (se sucesso)
     */
    public OperationResult updateStudent(
        String cpf,
        String newName,
        String newContact
    ) {
        String cleanCpf = Student.cleanCpf(cpf);

        for (Student student : students) {
            if (student.getCpf().equals(cleanCpf) && student.isActive()) {
                if (newName != null && !newName.trim().isEmpty()) {
                    student.setName(newName.trim());
                }
                if (newContact != null && !newContact.trim().isEmpty()) {
                    student.setContact(newContact.trim());
                }
                return new OperationResult(true,
                        "✅ Cadastro do aluno atualizado com sucesso!", student);
            }
        }

        return new OperationResult(false, "Nenhum aluno ativo encontrado com o CPF informado.");
    }

    /**
     * Lista todos os alunos ativos.
     *
     * @return OperationResult com ArrayList<Student> em data
     */
    public OperationResult listAll() {
        ArrayList<Student> activeStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.isActive()) {
                activeStudents.add(student);
            }
        }

        if (activeStudents.isEmpty()) {
            return new OperationResult(false, "Nenhum aluno cadastrado no sistema.");
        }

        return new OperationResult(true,
                activeStudents.size() + " aluno(s) encontrado(s).", activeStudents);
    }

    /**
     * Verifica se um CPF já está cadastrado no sistema (ativo ou inativo).
     */
    public boolean cpfExists(String cpf) {
        for (Student student : students) {
            if (student.getCpf().equals(cpf)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se existe um aluno ativo com o CPF informado.
     */
    public boolean hasActiveStudent(String cpf) {
        for (Student student : students) {
            if (student.getCpf().equals(cpf) && student.isActive()) {
                return true;
            }
        }
        return false;
    }
}