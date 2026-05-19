package ui.menus.reports;

public enum ReportsMenuOption {
    LISTAR_ALUNOS(1, "Listar todos os alunos"),
    LISTAR_PLANOS(2, "Listar todos os planos"),
    LISTAR_MATRICULAS(3, "Listar todas as matrículas"),
    MATRICULAS_ATIVAS(4, "Matrículas ativas"),
    MATRICULAS_CANCELADAS(5, "Matrículas canceladas"),
    SALDO_PENDENTE(6, "Matrículas com saldo pendente"),
    POR_TIPO_PLANO(7, "Matrículas por tipo de plano"),
    VENCIDAS(8, "Matrículas vencidas"),
    CONSULTAR_ALUNO(9, "Consultar aluno por CPF"),
    CONSULTAR_PLANO(10, "Consultar plano por nome"),
    CONSULTAR_MATRICULA(11, "Consultar matrícula ativa de um aluno"),
    ESTATISTICAS(12, "Estatísticas do sistema"),
    VOLTAR(13, "Voltar");

    private final int numero;
    private final String valorOpcao;

    ReportsMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    public int getNumber() { return numero; }
    public String getOptionName() { return valorOpcao; }

    public static ReportsMenuOption fromNumber(int numero) {
        for (ReportsMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}
