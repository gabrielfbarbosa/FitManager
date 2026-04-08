package ui.menus.reports;

public enum ReportsMenuOption {
    ALUNOS_ATIVOS(1, "Alunos com matrícula ativa"),
    SALDO_PENDENTE(2, "Matrículas com saldo pendente"),
    TODAS_MATRICULAS(3, "Todas as matrículas"),
    VOLTAR(4, "Voltar");

    private final int numero;
    private final String valorOpcao;

    ReportsMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    public int getNumber() { return numero; }
    public String getValorOpcao() { return valorOpcao; }

    public static ReportsMenuOption fromNumber(int numero) {
        for (ReportsMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}