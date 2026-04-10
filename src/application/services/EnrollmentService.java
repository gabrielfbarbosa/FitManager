package application.services;

/**
 * Serviço responsável por manter a coleção de matrículas em memória
 * e implementar as operações específicas da entidade Enrollment.
 */
public class EnrollmentService {

    public EnrollmentService() {
    }

    /**
     * Verifica se um aluno possui matrícula ativa.
     * Usado pelo FitManager para validar remoção de alunos e nova matrícula.
     *
     * @param cpf CPF do aluno (apenas dígitos)
     * @return true se o aluno possui ao menos uma matrícula ativa
     */
    public boolean hasActiveEnrollment(String cpf) {
        // Implementar função na criação da feature Enrollment-management
        return false;
    }
}