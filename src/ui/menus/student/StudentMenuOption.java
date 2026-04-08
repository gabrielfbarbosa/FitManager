package ui.menus.student;

public enum StudentMenuOption {
    CADASTRAR(1, "Cadastrar novo aluno"),
    CONSULTAR_CPF(2, "Consultar por CPF"),
    EDITAR(3, "Editar cadastro"),
    EXCLUIR(4, "Excluir aluno"),
    LISTAR(5, "Listar todos"),
    VOLTAR(6, "Voltar");

    private final int numero;
    private final String valorOpcao;

    StudentMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    public int getNumber() { return numero; }
    public String getValorOpcao() { return valorOpcao; }

    public static StudentMenuOption fromNumber(int numero) {
        for (StudentMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}