# FitManager

Sistema de gestão de academia desenvolvido em Java como projeto da disciplina de Programação Orientada a Objetos. O sistema permite o gerenciamento de alunos, planos, matrículas e pagamentos por meio de uma interface interativa baseada em JOptionPane.

---

## Integrantes

| Nome | GitHub |
|---|---|
| Gabriel Felipe Barbosa | https://github.com/gabrielfbarbosa |
| Marcelle Luna Souza | https://github.com/mahlunas |

---

## Tecnologias

- **Java 17 ou superior**
- Interface gráfica: `JOptionPane`
- Sem dependências externas — apenas a biblioteca padrão do Java

---

## Estrutura do Projeto

```
FitManager/
├── src/
│   ├── FitManagerApp.java              # Ponto de entrada do sistema
│   ├── application/
│   │   ├── FitManager.java             # Orquestrador central
│   │   ├── OperationResult.java        # Retorno padronizado das operações
│   │   └── services/
│   │       ├── StudentService.java
│   │       ├── PlanService.java
│   │       └── EnrollmentService.java
│   ├── domain/
│   │   └── model/
│   │       ├── Student.java
│   │       ├── Plan.java
│   │       ├── Enrollment.java
│   │       ├── Payment.java
│   │       └── enums/
│   │           ├── PlanType.java
│   │           ├── PaymentType.java
│   │           └── EnrollmentStatus.java
│   ├── mocks/
│   │   └── DataMock.java               # Dados de demonstração para testes
│   └── ui/
│       ├── menus/
│       │   ├── main/
│       │   │   ├── MainMenu.java
│       │   │   └── MainMenuOption.java
│       │   ├── student/
│       │   │   ├── StudentMenu.java
│       │   │   └── StudentMenuOption.java
│       │   ├── plan/
│       │   │   ├── PlanMenu.java
│       │   │   └── PlanMenuOption.java
│       │   ├── enrollment/
│       │   │   ├── EnrollmentMenu.java
│       │   │   └── EnrollmentMenuOption.java
│       │   └── reports/
│       │       ├── ReportsMenu.java
│       │       └── ReportsMenuOption.java
│       └── screen/
│           ├── UserInterface.java      # Toda a I/O do sistema
│           └── InputParser.java        # Validação e conversão de entradas
├── report.md
├── diagram.png
└── README.md
```

---

## Como Executar

### Pré-requisitos

- **Java 17 ou superior** instalado
- Uma IDE com suporte a Java
### Passos

1. Clone o repositório e abra o projeto na sua IDE.
2. Configure o diretório `src/` como raiz dos fontes (*Sources Root*).
3. Execute a classe `FitManagerApp` como ponto de entrada.

> O sistema inicia com dados de demonstração já carregados (`DEV_MODE = true` em `FitManagerApp`). Para começar com o sistema vazio, altere essa variável para `false`.

---

## Funcionalidades

- **Gestão de alunos** — cadastro, consulta por CPF, edição, inativação e listagem
- **Gestão de planos** — cadastro, consulta por nome, atualização de preço e listagem
- **Gestão de matrículas** — realização, registro de pagamentos, cancelamento, consulta e histórico
- **Relatórios** — alunos com matrícula ativa, matrículas com saldo pendente, todas as matrículas e estatísticas gerais
