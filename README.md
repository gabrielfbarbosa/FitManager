<div align="center">

# 🏋️ FitManager

**Sistema de gestão de academia desenvolvido em Java como projeto da disciplina de Linguagem de Programação Orientada a Objetos turma de 2026.**
O sistema permite o gerenciamento de alunos, planos, matrículas e pagamentos por meio de uma interface interativa com duas opções: **JOptionPane** (interface gráfica) ou **Terminal** (linha de comando). A partir da Etapa 3, todos os dados são **persistidos em arquivos** e recuperados entre sessões.

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
- Persistência em **arquivos de texto (CSV)** na pasta `data/` — sem banco de dados
- Sem dependências externas — apenas a biblioteca padrão do Java

---

## 📁 Estrutura do Projeto

```
FitManager/
├── src/
│   ├── FitManagerApp.java                  # Ponto de entrada — seleção de interface, carga e gravação
│   ├── application/
│   │   ├── FitManager.java                 # Fachada central (orquestra serviços e persistência)
│   │   ├── OperationResult.java            # Retorno padronizado genérico — OperationResult<T>
│   │   ├── services/
│   │   │   ├── StudentService.java
│   │   │   ├── PlanService.java
│   │   │   └── EnrollmentService.java
│   │   └── reports/
│   │       └── FinancialReport.java        # Resultado do relatório financeiro mensal
│   ├── domain/
│   │   └── model/
│   │       ├── Summarizable.java           # Interface polimórfica (getSummary)
│   │       ├── Student.java
│   │       ├── Enrollment.java
│   │       ├── enums/
│   │       │   ├── PlanType.java
│   │       │   ├── PaymentType.java
│   │       │   └── EnrollmentStatus.java
│   │       ├── plans/                      # Hierarquia de planos (abstrata + 4 subclasses + factory)
│   │       │   ├── Plan.java               # Classe abstrata
│   │       │   ├── MonthlyPlan.java
│   │       │   ├── QuarterlyPlan.java
│   │       │   ├── SemiAnnualPlan.java
│   │       │   ├── AnnualPlan.java
│   │       │   └── PlanFactory.java        # Instancia a subclasse correta (uso no serviço e na leitura)
│   │       ├── payments/                   # Hierarquia de pagamentos (abstrata + 4 subclasses + factory)
│   │       │   ├── Payment.java            # Classe abstrata
│   │       │   ├── PixPayment.java
│   │       │   ├── CreditCardPayment.java
│   │       │   ├── DebitCardPayment.java
│   │       │   ├── CashPayment.java
│   │       │   └── PaymentFactory.java     # Reconstrói o tipo concreto na leitura de arquivo
│   │       └── filters/                    # Filtros polimórficos de matrículas
│   │           ├── EnrollmentFilter.java   # Interface
│   │           ├── ActiveEnrollmentFilter.java
│   │           ├── CancelledEnrollmentFilter.java
│   │           ├── PendingBalanceFilter.java
│   │           ├── ByPlanTypeFilter.java
│   │           └── ExpiredEnrollmentFilter.java
│   ├── persistence/                        # Camada de persistência em arquivos
│   │   ├── Repository.java                 # Classe genérica abstrata — Repository<T> (save()/load() abstratos)
│   │   ├── StudentRepository.java
│   │   ├── PlanRepository.java
│   │   └── EnrollmentRepository.java       # Matrículas + pagamentos + diretiva nextCode
│   ├── exceptions/                         # Hierarquia de exceções personalizadas
│   │   ├── FitManagerException.java        # Raiz do domínio (não verificada)
│   │   ├── ValidationException.java
│   │   ├── RequiredFieldException.java
│   │   ├── InvalidFormatFieldException.java
│   │   ├── BusinessException.java
│   │   ├── StudentWithActiveEnrollmentException.java
│   │   ├── DuplicatedEnrollmentException.java
│   │   ├── DuplicatedPlanException.java
│   │   ├── PersistenceException.java       # Raiz de persistência (verificada, separada)
│   │   ├── CorruptedFileException.java
│   │   └── WriteFailureException.java
│   ├── mocks/
│   │   └── DataMock.java                   # Dados de demonstração (DEV_MODE)
│   ├── ui/
│   │   ├── menus/
│   │   │   ├── main/        (MainMenu, MainMenuOption)
│   │   │   ├── student/     (StudentMenu, StudentMenuOption)
│   │   │   ├── plan/        (PlanMenu, PlanMenuOption)
│   │   │   ├── enrollment/  (EnrollmentMenu, EnrollmentMenuOption)
│   │   │   └── reports/     (ReportsMenu, ReportsMenuOption)
│   │   └── screen/
│   │       ├── UserInterface.java          # Contrato de I/O (getInt/getDouble/getDate/showMenu/...)
│   │       ├── BaseUserInterface.java      # Loops de validação compartilhados
│   │       ├── JOptionPaneUI.java          # Implementação gráfica (JOptionPane)
│   │       └── TerminalUI.java             # Implementação via terminal
│   └── util/
│       ├── CurrencyFormatter.java          # Formatação de valores em BRL (R$ 1.234,56)
│       ├── DateFormatter.java              # Formatação de datas/horas no padrão BR (dd/MM/yyyy)
│       └── UserInputParser.java            # Interface funcional genérica de parsing — UserInputParser<T>
├── data/                                   # Arquivos de dados (criados na 1ª gravação)
│   ├── students.txt
│   ├── plans.txt
│   ├── enrollments.txt
│   ├── payments.txt
│   └── reports/                            # Relatórios financeiros exportados (.csv)
├── report.md
├── diagram.puml / diagram.png
└── README.md
```

---

## 🚀 Como Executar

### Pré-requisitos

- **JDK 25** instalado (`java --version` deve indicar 25.x).
- Opcional: uma IDE com suporte a Java (IntelliJ IDEA, Eclipse, VS Code).

### Executando o projeto

1. Clone o repositório e abra o projeto.
2. Configure o diretório `src/` como raiz dos fontes (*Sources Root*).
3. Execute a classe `FitManagerApp` como ponto de entrada.

### Na inicialização

1. Escolha a interface: **Interface Gráfica (JOptionPane)** ou **Terminal (Linha de Comando)**. Fechar o diálogo encerra o programa.
2. O sistema carrega automaticamente os dados da pasta `data/` (se existirem) e exibe o menu principal já com o estado restaurado.

> 💡 **Primeira execução / sistema vazio:** se ainda não houver arquivos em `data/`, o sistema inicia vazio (situação normal) e exibe apenas uma dica informando que existe a constante `DEV_MODE` em `FitManagerApp`. Definindo `DEV_MODE = true`, o sistema é populado automaticamente com dados de demonstração (`DataMock`) sempre que iniciar vazio — útil para explorar as funcionalidades sem cadastrar nada manualmente. Em uso normal, mantenha `DEV_MODE = false`.

---

## 💾 Persistência de Dados

A partir da Etapa 3, todo o estado do sistema é gravado em arquivos de **texto (CSV, delimitador `;`)** na pasta `data/`, relativa ao diretório de execução:

| Arquivo | Conteúdo | Formato por linha |
|---|---|---|
| `data/students.txt` | Alunos | `cpf;name;contact;birthDate;active;removedAt` |
| `data/plans.txt` | Planos (tipo concreto no 1º campo) | `type;name;description;minimumDuration;pricePerMonth` |
| `data/enrollments.txt` | Matrículas (+ diretiva `# nextCode=N`) | `code;studentCpf;planName;startDate;durationMonths;totalPrice;status;cancelledAt` |
| `data/payments.txt` | Pagamentos (tipo no 2º campo) | `enrollmentCode;type;amount;paymentDate;description;extra1;extra2` |

Comportamento:

- **Carga (inicialização):** alunos e planos são carregados antes das matrículas (que os referenciam por CPF e nome). O `nextCode` das matrículas é restaurado para que novos códigos não se repitam.
- **Gravação (encerramento):** ao escolher "Sair", todos os dados são gravados na ordem inversa. Recursos de arquivo são sempre fechados via *try-with-resources*.
- **Arquivo ausente:** tratado como primeira execução — o sistema inicia vazio, sem erro.
- **Arquivo corrompido / inconsistente:** a leitura lança uma exceção de persistência, e o sistema informa o arquivo afetado ao usuário sem encerrar abruptamente.
- **Polimorfismo preservado:** os tipos concretos de `Plan` e `Payment` são reconstruídos na leitura (via `PlanFactory`/`PaymentFactory`), mantendo o comportamento específico de cada subclasse após a recarga.
- Linhas iniciadas por `#` são comentários/cabeçalhos e são ignoradas na leitura.

---

## ✅ Funcionalidades

- 👤 **Gestão de alunos** — cadastro, consulta por CPF, edição, inativação (soft delete) e listagem
- 📋 **Gestão de planos** — cadastro com 4 tipos (Mensal, Trimestral, Semestral, Anual), consulta por nome, atualização de preço e listagem
- 📝 **Gestão de matrículas** — realização, registro de pagamentos (PIX, Cartão de Crédito, Cartão de Débito, Dinheiro), cancelamento com taxa informativa e histórico por aluno
- 📊 **Relatórios** — 13 opções: listagens, filtros polimórficos (ativas, canceladas, vencidas, com saldo pendente, por tipo de plano), consultas individuais, estatísticas gerais e o **relatório financeiro mensal**
- 💰 **Relatório financeiro mensal** — receita total, receita por tipo de plano e por forma de pagamento, total de taxas de processamento, matrículas iniciadas/canceladas e ranking de planos mais contratados; com exportação opcional para CSV em `data/reports/`
- 🛡️ **Validação robusta de entradas** — todo campo numérico, decimal e de data é protegido; entradas inválidas exibem o formato esperado e pedem nova tentativa, sem nunca encerrar o programa
- 🖥️ **Interface dupla** — escolha entre JOptionPane (gráfica) ou Terminal (linha de comando) na inicialização, com comportamento idêntico em ambas

> ℹ️ **Regra de negócio:** a FitManager foi inaugurada em **01/03/2026** — o relatório financeiro só é gerado para períodos a partir dessa data (a partir de 2027, todos os meses ficam disponíveis).

---

## 🏗️ Conceitos de POO Aplicados

**Etapas 1 e 2**

- **Classes abstratas**: `Plan` e `Payment` com métodos abstratos e concretos
- **Herança**: 4 subclasses de `Plan` e 4 de `Payment`
- **Interfaces**: `Summarizable` (exibição polimórfica), `EnrollmentFilter` (filtros) e `UserInterface` (abstração de I/O)
- **Polimorfismo**: cálculo de preços, taxas e filtros sem condicionais — cada subclasse define seu próprio comportamento
- **Encapsulamento**: atributos privados, acesso via getters/setters, validações internas nos serviços
- **Enums**: `PlanType`, `PaymentType` e `EnrollmentStatus` com labels em português
- **Generics**: `OperationResult<T>` parametrizado (sem casts nos menus); classe genérica `Repository<T>`; interface funcional `UserInputParser<T>`; coleções tipadas em todo o código (`ArrayList<Student>`, `Map<String, Double>`)
- **Tratamento de exceções**: hierarquia personalizada em categorias — validação e regra de negócio (não verificadas, sob `FitManagerException`) e persistência (verificada, sob `PersistenceException`), com captura por camada
- **Persistência em arquivos**: gravação/leitura em CSV com preservação dos tipos polimórficos e do `nextCode`, usando *try-with-resources*
- **Coleções genéricas**: agregações do relatório financeiro em `Map<String, Double>` agrupadas por `getTypeName()`, sem `instanceof`

---

## 🧱 Arquitetura em Camadas

```
ui/            Interface com o usuário (menus + telas). Não contém regra de negócio.
application/   Orquestração (FitManager), serviços de domínio e o relatório financeiro.
domain/        Entidades, hierarquias de Plan/Payment, enums e filtros.
persistence/   Repositórios genéricos — leitura e escrita em arquivo.
exceptions/    Hierarquia de exceções personalizadas.
util/          Formatação (moeda, data) e parsing genérico de entradas.
```

Regra de dependência: o domínio não conhece persistência; os menus capturam exceções de domínio (validação/negócio), enquanto as exceções de persistência são tratadas na inicialização/encerramento pelo `FitManagerApp`/`FitManager`.
