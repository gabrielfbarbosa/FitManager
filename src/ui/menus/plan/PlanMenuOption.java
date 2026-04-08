package ui.menus.plan;

public enum PlanMenuOption {
    CADASTRAR(1, "Cadastrar novo plano"),
    CONSULTAR_NOME(2, "Consultar por nome"),
    ALTERAR_PRECO(3, "Alterar preço"),
    LISTAR(4, "Listar todos"),
    VOLTAR(5, "Voltar");

    private final int numero;
    private final String valorOpcao;

    PlanMenuOption(int numero, String valorOpcao) {
        this.numero = numero;
        this.valorOpcao = valorOpcao;
    }

    public int getNumber() { return numero; }
    public String getValorOpcao() { return valorOpcao; }

    public static PlanMenuOption fromNumber(int numero) {
        for (PlanMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}