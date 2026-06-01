package ui.menus.student;

import application.FitManager;
import application.OperationResult;
import domain.model.Student;
import exceptions.FitManagerException;
import ui.screen.UserInterface;
import util.DateFormatter;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Menu de gerenciamento de alunos.
 * Apresenta as opções e encaminha solicitações ao FitManager.
 *
 * Tratamento de exceções: cada fluxo captura {@link FitManagerException} —
 * categoria base das exceções não verificadas do sistema — e exibe a mensagem
 * ao usuário via {@code ui.showError()}. Assim, nenhuma falha de validação ou
 * de regra de negócio chega ao terminal como stack trace.
 */
public class StudentMenu {

    private UserInterface ui;
    private FitManager fitManager;

    public StudentMenu(UserInterface ui, FitManager fitManager) {
        this.ui = ui;
        this.fitManager = fitManager;
    }

    /**
     * Loop principal do menu de alunos.
     * Exibe opções até o usuário escolher "Voltar".
     */
    public void run() {
        boolean running = true;

        while (running) {
            try {
                StringBuilder menuOptions = new StringBuilder();
                for (StudentMenuOption opt : StudentMenuOption.values()) {
                    menuOptions.append(opt.getNumber()).append(" - ").append(opt.getValorOpcao()).append("\n");
                }
                Integer choice = ui.showMenu("> GERENCIAR ALUNOS", menuOptions.toString(), StudentMenuOption.values().length);

                if (choice == null) { running = false; continue; }

                StudentMenuOption option = StudentMenuOption.fromNumber(choice);
                if (option == null) continue;

                switch (option) {
                    case CADASTRAR:     registerStudent();      break;
                    case CONSULTAR_CPF: findStudentByCpf();     break;
                    case EDITAR:        editStudent();          break;
                    case EXCLUIR:       removeStudent();        break;
                    case LISTAR:        listAllStudents();      break;
                    case VOLTAR:        running = false;        break;
                }
            } catch (FitManagerException e) {
                ui.showError(e.getMessage());
            }
        }
    }

    /**
     * Fluxo de cadastro de novo aluno.
     * Coleta dados via UserInterface e delega ao FitManager.
     */
    private void registerStudent() {
        String name = ui.getInput("Digite o nome:", "Nome");
        if (name == null) return;

        String cpf = askValidCpf("Digite o CPF (apenas números):");
        if (cpf == null) return;

        String contact = ui.getInput("Digite o contato (e-mail ou telefone):", "Contato");
        if (contact == null) return;

        LocalDate birthDate = ui.getDate("Digite a data de nascimento (dia/mê/ano ex: 30/07/1993):", "Data de Nascimento");
        if (birthDate == null) return;

        String birthDateStr = DateFormatter.formatDate(birthDate);
        OperationResult<Student> result = fitManager.registerStudent(name, cpf, contact, birthDateStr);

        if (result.isSuccess()) {
            Student student = result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados:\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de consulta de aluno por CPF.
     */
    private void findStudentByCpf() {
        String cpf = askValidCpf("Digite o CPF para consulta:");
        if (cpf == null) return;

        OperationResult<Student> result = fitManager.findStudentByCpf(cpf);

        if (result.isSuccess()) {
            Student student = result.getData();
            ui.showMessage("Aluno encontrado:\n\n" + student.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    private String askValidCpf(String prompt) {

        while (true) {

            String cpf = ui.getInput(prompt, "CPF");

            if (cpf == null) return null;

            OperationResult<String> result = fitManager.validateCpf(cpf);

            if (result.isSuccess()) {
                return result.getData();
            }

            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de edição de cadastro do aluno.
     * Permite alterar nome e contato. Campos deixados em branco mantêm o valor atual.
     */
    private void editStudent() {
        String cpf = askValidCpf("Digite o CPF do aluno a editar:");
        if (cpf == null) return;

        // Primeiro verifica se o aluno existe
        OperationResult<Student> findResult = fitManager.findStudentByCpf(cpf);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Student currentStudent = findResult.getData();
        ui.showMessage("Aluno encontrado:\n\n" + currentStudent.toString() +
                "\n\nDeixe em branco os campos que não deseja alterar.");

        String newName = ui.getInput("Novo nome (atual: " + currentStudent.getName() + "):");
        if (newName == null) return;

        String newContact = ui.getInput("Novo contato (atual: " + currentStudent.getContact() + "):");
        if (newContact == null) return;

        OperationResult<Student> result = fitManager.updateStudent(cpf, newName, newContact);

        if (result.isSuccess()) {
            Student updated = result.getData();
            ui.showMessage(result.getMessage() + "\n\nDados atualizados:\n" + updated.toString());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de remoção (inativação) de aluno.
     */
    private void removeStudent() {
        String cpf = askValidCpf("Digite o CPF do aluno a remover:");
        if (cpf == null) return;

        // Mostra o aluno antes de confirmar a remoção
        OperationResult<Student> findResult = fitManager.findStudentByCpf(cpf);
        if (!findResult.isSuccess()) {
            ui.showError(findResult.getMessage());
            return;
        }

        Student student = findResult.getData();
        String confirm = ui.getInput(
                "Confirma a remoção do aluno?\n\n" + student.toString() +
                        "\n\nDigite 'S' para confirmar ou qualquer outra tecla para cancelar:");

        if (confirm == null || !confirm.trim().equalsIgnoreCase("S")) {
            ui.showMessage("Operação cancelada.");
            return;
        }

        OperationResult<Void> result = fitManager.removeStudent(cpf);

        if (result.isSuccess()) {
            ui.showMessage(result.getMessage());
        } else {
            ui.showError(result.getMessage());
        }
    }

    /**
     * Fluxo de listagem de todos os alunos ativos.
     */
    private void listAllStudents() {
        OperationResult<ArrayList<Student>> result = fitManager.listAllStudents();

        if (!result.isSuccess()) {
            ui.showError(result.getMessage());
            return;
        }

        ArrayList<Student> students = result.getData();
        String message = "> ALUNOS CADASTRADOS \n";
        message += "Total: " + students.size() + " aluno(s)\n\n";

        for (int i = 0; i < students.size(); i++) {
            message += "--- Aluno " + (i + 1) + " ---\n";
            message += students.get(i).toString();
            if (i < students.size() - 1) {
                message += "\n\n";
            }
        }

        ui.showScrollableMessage(message);
    }
}
