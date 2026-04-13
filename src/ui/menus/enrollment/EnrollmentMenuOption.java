package ui.menus.enrollment;

public enum EnrollmentMenuOption {
    MATRICULAR(1, "Realizar matrícula"),
    REGISTRAR_PAGAMENTO(2, "Registrar pagamento"),
    CANCELAR(3, "Cancelar matrícula"),
    CONSULTAR_ATIVA(4, "Consultar matrícula ativa"),
    HISTORICO(5, "Listar histórico"),
    VOLTAR(6, "Voltar");

    private final int numero;
    private final String valorOpcao;

    EnrollmentMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    public int getNumber() { return numero; }
    public String getValorOpcao() { return valorOpcao; }

    public static EnrollmentMenuOption fromNumber(int numero) {
        for (EnrollmentMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}
