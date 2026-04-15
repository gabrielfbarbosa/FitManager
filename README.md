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

-  **Java 25.0.1**
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
│   │       ├── UserInterface.java      # Toda a I/O do sistema
│   │       └── InputParser.java        # Validação e conversão de entradas
│   └── util/
│       ├── CurrencyFormatter.java      # Formatação de valores em BRL (R$ 1.234,56)
│       └── DateFormatter.java          # Formatação de datas/horas no padrão BR (dd/MM/yyyy)
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

---

## 🧰 Utilitários de formatação (`util/`)

Para evitar duplicação de código e garantir consistência de exibição em todas as telas/relatórios, o pacote `util` concentra as formatações que eram antes escritas manualmente (`String.format("%.2f", ...)`, `String.format("%02d/%02d/%04d", ...)` etc.).

### `CurrencyFormatter`

Formata valores monetários no padrão brasileiro usando `java.text.NumberFormat` com `Locale("pt","BR")` — ponto como separador de milhar e vírgula como separador decimal.

| Método | Exemplo |
|---|---|
| `CurrencyFormatter.format(2596.5)` | `"R$ 2.596,50"` |
| `CurrencyFormatter.formatCurrency(2596.5)` | `"R$ 2.596,50"` (instância de moeda do Locale) |
| `CurrencyFormatter.formatNumber(2596.5)` | `"2.596,50"` (sem símbolo) |

### `DateFormatter`

Formata datas, horas e timestamps no padrão brasileiro usando `java.time.format.DateTimeFormatter` com `Locale("pt","BR")`. Trabalha com `LocalDate`, `LocalTime` e `LocalDateTime`.

| Padrão | Exemplo |
|---|---|
| `DateFormatter.format(LocalDate)` → `dd/MM/yyyy` | `"14/04/2026"` |
| `DateFormatter.format(LocalTime)` → `HH:mm:ss` | `"09:30:45"` |
| `DateFormatter.format(LocalDateTime)` → `dd/MM/yyyy HH:mm:ss` | `"14/04/2026 09:30:45"` |
| `DateFormatter.parseDate(String)` | converte `"14/04/2026"` em `LocalDate` |

Também expõe as constantes `DATE_PATTERN`, `TIME_PATTERN`, `DATE_TIME_PATTERN` e os `DateTimeFormatter` públicos para situações específicas (ex.: parse com tolerância a variações).
