package ui.menus.main;

public enum MainMenuOption {
    GERENCIAR_ALUNOS(1, "Gerenciar alunos"),
    GERENCIAR_PLANOS(2, "Gerenciar planos"),
    GERENCIAR_MATRICULAS(3, "Gerenciar matrículas"),
    RELATORIOS(4, "Relatórios / listagens"),
    SAIR(5, "Sair");

    private final int numero;
    private final String optionName;

    MainMenuOption(int numero, String optionName) {
        this.numero = numero;
        this.optionName = optionName;
    }

    public int getNumber() { return numero; }
    public String getOptionName() { return optionName; }

    public static MainMenuOption fromNumber(int numero) {
        for (MainMenuOption option : values()) {
            if (option.getNumber() == numero) {
                return option;
            }
        }
        return null;
    }
}