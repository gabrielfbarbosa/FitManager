<div align="center">

# 🏋️ FitManager

**Sistema de gestão de academia desenvolvido em Java como projeto da disciplina de Linguagem de Programação Orientada a Objetos turma de 2026.**
O sistema permite o gerenciamento de alunos, planos, matrículas e pagamentos por meio de uma interface interativa baseada em JOptionPane.

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

-  **Java 17 ou superior**
- Interface gráfica: `JOptionPane`
- Sem dependências externas — apenas a biblioteca padrão do Java

---

## 📁 Estrutura do Projeto

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

## 🚀 Como Executar

### Pré-requisitos

- **Java 17 ou superior** instalado
- Uma IDE com suporte a Java
### Passos

1. Clone o repositório e abra o projeto na sua IDE.
2. Configure o diretório `src/` como raiz dos fontes (*Sources Root*).
3. Execute a classe `FitManagerApp` como ponto de entrada.

> 💡 O sistema inicia com dados de demonstração já carregados (`DEV_MODE = true` em `FitManagerApp`). Para começar com o sistema vazio, altere essa variável para `false`.

---

## ✅ Funcionalidades

- 👤 **Gestão de alunos** — cadastro, consulta por CPF, edição, inativação e listagem
- 📋 **Gestão de planos** — cadastro, consulta por nome, atualização de preço e listagem
- 📝 **Gestão de matrículas** — realização, registro de pagamentos, cancelamento, consulta e histórico
- 📊 **Relatórios** — alunos com matrícula ativa, matrículas com saldo pendente, todas as matrículas e estatísticas gerais
