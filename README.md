<div align="center">

# 🏋️ FitManager

**Sistema de gestão de academia desenvolvido em Java como projeto da disciplina de Linguagem de Programação Orientada a Objetos turma de 2026.**
O sistema permite o gerenciamento de alunos, planos, matrículas e pagamentos por meio de uma interface interativa com duas opções: **JOptionPane** (interface gráfica) ou **Terminal** (linha de comando).

</div>

---

## Integrantes


|  [<img loading="lazy" style="border-radius: 50%;" src="https://github.com/gabrielfbarbosa.png" width=115><br><sub><b>Gabriel Felipe Barbosa</b></sub>](https://github.com/gabrielfbarbosa)  |  [<img loading="lazy" style="border-radius: 50%;" src="https://github.com/mahlunas.png" width=115><br><sub><b>Marcelle Luna Souza</b></sub>](https://github.com/mahlunas)   |
|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------:|

> 🎓 **Professor Dr. responsável da disciplina:**
> 
> [<img loading="lazy" style="border-radius: 50%;" src="https://github.com/lordantonelli.png" width=115><br><sub><b>Humberto Lidio Antonelli</b></sub>](https://github.com/lordantonelli)

---

## 🛠️ Tecnologias

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![IntelliJ IDEA](https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white)

- **Java 25.0.1**
- Interfaces: `JOptionPane` (gráfica) e `Terminal` (linha de comando)
- Sem dependências externas — apenas a biblioteca padrão do Java

---

## 📁 Estrutura do Projeto

```
FitManager/
├── src/
│   ├── FitManagerApp.java                  # Ponto de entrada — seleção de interface
│   ├── application/
│   │   ├── FitManager.java                 # Fachada central (coordena os serviços)
│   │   ├── OperationResult.java            # Retorno padronizado das operações
│   │   └── services/
│   │       ├── StudentService.java
│   │       ├── PlanService.java
│   │       └── EnrollmentService.java
│   ├── domain/
│   │   └── model/
│   │       ├── Summarizable.java           # Interface polimórfica (getSummary)
│   │       ├── Student.java
│   │       ├── Enrollment.java
│   │       ├── enums/
│   │       │   ├── PlanType.java
│   │       │   ├── PaymentType.java
│   │       │   └── EnrollmentStatus.java
│   │       ├── plans/                      # Hierarquia de planos (classe abstrata + 4 subclasses)
│   │       │   ├── Plan.java               # Classe abstrata
│   │       │   ├── MonthlyPlan.java
│   │       │   ├── QuarterlyPlan.java
│   │       │   ├── SemiAnnualPlan.java
│   │       │   └── AnnualPlan.java
│   │       ├── payments/                   # Hierarquia de pagamentos (classe abstrata + 4 subclasses)
│   │       │   ├── Payment.java            # Classe abstrata
│   │       │   ├── PixPayment.java
│   │       │   ├── CreditCardPayment.java
│   │       │   ├── DebitCardPayment.java
│   │       │   └── CashPayment.java
│   │       └── filters/                    # Filtros polimórficos de matrículas
│   │           ├── EnrollmentFilter.java   # Interface
│   │           ├── ActiveEnrollmentFilter.java
│   │           ├── CancelledEnrollmentFilter.java
│   │           ├── PendingBalanceFilter.java
│   │           ├── ByPlanTypeFilter.java
│   │           └── ExpiredEnrollmentFilter.java
│   ├── mocks/
│   │   └── DataMock.java                   # Dados de demonstração (DEV_MODE)
│   ├── ui/
│   │   ├── menus/
│   │   │   ├── main/
│   │   │   │   ├── MainMenu.java
│   │   │   │   └── MainMenuOption.java
│   │   │   ├── student/
│   │   │   │   ├── StudentMenu.java
│   │   │   │   └── StudentMenuOption.java
│   │   │   ├── plan/
│   │   │   │   ├── PlanMenu.java
│   │   │   │   └── PlanMenuOption.java
│   │   │   ├── enrollment/
│   │   │   │   ├── EnrollmentMenu.java
│   │   │   │   └── EnrollmentMenuOption.java
│   │   │   └── reports/
│   │   │       ├── ReportsMenu.java
│   │   │       └── ReportsMenuOption.java
│   │   └── screen/
│   │       ├── UserInterface.java          # Interface Java (contrato de I/O)
│   │       ├── JOptionPaneUI.java          # Implementação gráfica (JOptionPane)
│   │       ├── TerminalUI.java             # Implementação via terminal
│   │       └── InputParser.java            # Validação e conversão de entradas
│   └── util/
│       ├── CurrencyFormatter.java          # Formatação de valores em BRL (R$ 1.234,56)
│       └── DateFormatter.java              # Formatação de datas/horas no padrão BR (dd/MM/yyyy)
├── report.md
├── diagram.puml
├── diagram.svg
└── README.md
```

---

## 🚀 Como Executar

### Pré-requisitos

- **Java 17 ou superior** instalado
- Uma IDE com suporte a Java
### Passos

1. Clone o repositório e abra o projeto na sua IDE.
2. Configure o diretório `src/` como raiz dos fontes (*Sources Root*).
3. Execute a classe `FitManagerApp` como ponto de entrada.
4. Na tela inicial, escolha a interface desejada: **Interface Gráfica (JOptionPane)** ou **Terminal (Linha de Comando)**.

> 💡 O sistema inicia com dados de demonstração já carregados (`DEV_MODE = true` em `FitManagerApp`). Para começar com o sistema vazio, altere essa variável para `false`.
>
> 💡 A seleção de interface é feita via `JOptionPane.showOptionDialog` antes de qualquer outra tela. Fechar o diálogo encerra o programa.

---

## ✅ Funcionalidades

- 👤 **Gestão de alunos** — cadastro, consulta por CPF, edição, inativação (soft delete) e listagem
- 📋 **Gestão de planos** — cadastro com 4 tipos (Mensal, Trimestral, Semestral, Anual), consulta por nome, atualização de preço e listagem
- 📝 **Gestão de matrículas** — realização, registro de pagamentos (PIX, Cartão de Crédito, Cartão de Débito, Dinheiro), cancelamento com taxa informativa e histórico por aluno
- 📊 **Relatórios** — 13 opções incluindo filtros polimórficos (matrículas ativas, canceladas, vencidas, com saldo pendente, por tipo de plano), consultas individuais e estatísticas gerais do sistema
- 🖥️ **Interface dupla** — escolha entre JOptionPane (gráfica) ou Terminal (linha de comando) na inicialização, com comportamento idêntico em ambas

---

## 🏗️ Conceitos de POO Aplicados

- **Classes abstratas**: `Plan` e `Payment` com métodos abstratos e concretos
- **Herança**: 4 subclasses de Plan (MonthlyPlan, QuarterlyPlan, SemiAnnualPlan, AnnualPlan) e 4 de Payment (PixPayment, CreditCardPayment, DebitCardPayment, CashPayment)
- **Interfaces**: `Summarizable` (exibição polimórfica), `EnrollmentFilter` (filtros polimórficos) e `UserInterface` (abstração de I/O)
- **Polimorfismo**: cálculo de preços, taxas e filtros sem condicionais — cada subclasse define seu próprio comportamento
- **Encapsulamento**: atributos privados, acesso via getters/setters, validações internas nos serviços
- **Enums**: `PlanType`, `PaymentType` e `EnrollmentStatus` com labels em português

