# Relatório — FitManager (Etapa 1)

## 1. Introdução

O **FitManager** é um sistema de gestão de academia desenvolvido em Java, implementado como projeto da disciplina de Programação Orientada a Objetos. Nesta primeira etapa, o sistema modela o domínio de uma academia — alunos, planos, matrículas e pagamentos — e implementa as operações essenciais de cadastro, consulta, atualização, remoção, matrícula, pagamento e cancelamento, além de relatórios consolidados.

A aplicação segue uma arquitetura em três camadas (UI, Application e Domain), com orquestração centralizada na classe `FitManager`, retornos padronizados via `OperationResult` e persistência apenas em memória durante a execução. A interface com o usuário é baseada em `JOptionPane`.

---

## 2. Integrantes e contribuições

| Nome | GitHub | Principais contribuições |
|---|---|---|
| Gabriel Felipe Barbosa | https://github.com/gabrielfbarbosa | Arquitetura do sistema, classe `FitManager`, serviços (`StudentService`, `PlanService`, `EnrollmentService`), `OperationResult`, `InputParser` e validação de CPF com dígito verificador. |
| Marcelle Luna Souza | https://github.com/mahlunas | Camada de interface (`UserInterface`, menus), fluxos de interação, classes de domínio (`Student`, `Plan`, `Enrollment`, `Payment`), enums e dados de demonstração (`DataMock`). |

A divisão foi feita por camada, mas houve colaboração contínua em revisões de pull request e integrações entre camadas, conforme histórico de commits.

---

## 3. Diagrama de classes final

O diagrama final encontra-se no arquivo `diagram.png` na raiz do repositório. Ele reflete fielmente o código implementado, com as seguintes adições em relação ao diagrama original:

- Campo `removedAt: LocalDate` em `Student` — registra a data de inativação.
- Campo `cancelledAt: LocalDate` em `Enrollment` — registra a data do cancelamento.
- Constante `DISCOUNT_RATE` em `Plan` — suporte ao desconto progressivo por duração contratada.
- Método `getMonthsRemaining()` em `Enrollment` — utilitário para relatórios de vencimento.
- Método `isExpired()` em `Enrollment` — utilitário para detecção de matrículas vencidas.
- Classe `InputParser` em `ui.screen` — utilitário estático de parsing seguro (sem exceções não tratadas).
- Pacote `util` com as classes `CurrencyFormatter` e `DateFormatter` — utilitários estáticos para formatação de valores monetários (BRL) e de datas/horas no padrão brasileiro.

Essas adições foram feitas por necessidade das funcionalidades implementadas e são descritas nas decisões de projeto abaixo.

---

## 4. Decisões de projeto

Esta seção responde, em ordem, a cada reflexão sinalizada no enunciado, descrevendo a decisão tomada, as alternativas consideradas, a justificativa e o impacto.

### 4.1 Instanciação dos menus (lazy vs eager)

**Decisão:** instanciação tardia (*lazy instantiation*) dos submenus no `MainMenu`.

**Alternativas:** criar todos os submenus no construtor do `MainMenu` e passá-los por dependência, ou criar um novo menu a cada acesso.

**Justificativa:** cada submenu é criado apenas na primeira vez que o usuário acessa a opção correspondente, por meio de *getters* privados (`getStudentMenu()`, `getPlanMenu()`, etc.) que verificam se o campo é nulo antes de instanciar. Isso evita construir objetos que podem nunca ser usados em uma sessão curta e, ao mesmo tempo, reaproveita a mesma instância em chamadas seguintes — mantendo estado consistente e referências únicas para `UserInterface` e `FitManager`.

**Impacto:** o `MainMenu` tem quatro campos privados de menu que começam nulos e são preenchidos sob demanda. A abordagem é simples, tem baixo acoplamento e facilita testes isolados.

### 4.2 Uso do campo `data` em `OperationResult`

**Decisão:** usar o campo `data` sempre que o chamador tiver benefício direto em reutilizar o objeto produzido pela operação.

**Casos concretos no projeto:**
- `registerStudent` e `updateStudent` → retornam o `Student` criado/atualizado.
- `findByCpf` → retorna o `Student` encontrado.
- `registerPlan`, `findByName`, `updatePrice` → retornam o `Plan`.
- `enroll`, `findActiveByStudentCpf` → retornam o `Enrollment`.
- `registerPayment` → retorna o `Payment` registrado.
- `listAll`, `listActive`, `listWithPendingBalance`, `listHistoryByStudent` → retornam `ArrayList` da respectiva entidade.

Operações com efeito puramente confirmatório (ex.: `cancelEnrollment`, `removeStudent`) não utilizam o campo `data`, pois o menu não precisa dos objetos para exibir a confirmação.

**Impacto:** a interface evita buscas redundantes após uma operação de escrita, e o padrão fica uniforme para a futura transição de `Object` para um tipo genérico `T`.

### 4.3 Responsabilidades da classe `Enrollment`

**Decisão:** concentrar em `Enrollment` apenas o que depende do próprio objeto — lista de pagamentos, cálculo de saldo (`calculateBalance`), status e datas —, delegando a coordenação ao serviço.

**Observação durante a implementação:** `cancel()` e `calculateBalance()` permaneceram curtos e coesos. A validação de estado (`hasActiveEnrollment`, rejeição de pagamento em matrícula cancelada) foi alocada no `EnrollmentService`, que é onde a regra faz sentido por envolver contexto fora de uma única matrícula.

**Impacto:** `Enrollment` ficou com 9 métodos e nenhum ultrapassa 15 linhas. A regra "se a operação usa dados de apenas um objeto, ela mora naquele objeto" foi aplicada consistentemente.

### 4.4 Estratégia de remoção de alunos — remoção física vs. inativação

**Decisão:** **inativação lógica** via `deactivate()`, que seta `active = false` e registra `removedAt`.

**Alternativas:** remoção física da lista interna do `StudentService`.

**Justificativa:** o histórico de matrículas anteriores de um aluno deve ser preservado integralmente. Excluir fisicamente o `Student` deixaria as referências em `Enrollment.studentCpf` órfãs em relação a qualquer dado textual do aluno (nome, contato). A inativação preserva todos os objetos, e as listagens de alunos filtram por `isActive()`. O campo `removedAt` foi adicionado para registrar quando a inativação ocorreu — enriquece relatórios futuros.

**Impacto:** `StudentService.listAll()` e `findByCpf()` retornam apenas alunos ativos; `cpfExists()` considera ativos e inativos para bloquear a reutilização de CPF. A regra obrigatória (aluno com matrícula ativa não pode ser removido) é verificada pelo `FitManager.removeStudent()` antes de delegar ao serviço.

### 4.5 Profundidade e formato de armazenamento do CPF

**Decisão:** validação completa com dígito verificador (módulo 11) e armazenamento **sem formatação** (apenas 11 dígitos).

**Alternativas:** validar apenas comprimento/caracteres numéricos; armazenar com pontos e traço.

**Justificativa:** a verificação de dígito verificador bloqueia CPFs estruturalmente inválidos antes que eles entrem no sistema, o que aumenta significativamente a robustez sem custo proibitivo — o método `validateCpf` é estático e não depende de nenhum estado. CPFs como `"111.111.111-11"` também são rejeitados via `allDigitsEqual`. O armazenamento sem formatação simplifica busca e comparação (todas as operações chamam `Student.cleanCpf` na entrada), e a exibição usa `getFormattedCpf()` apenas no `toString()`. O método `cleanCpf` é estático e utilizado como ponto único de normalização em `FitManager` e nos serviços.

**Impacto:** todas as buscas usam a forma canônica (11 dígitos puros). A exibição ao usuário permanece amigável (`123.456.789-00`).

### 4.6 Desconto por duração contratada

**Decisão:** implementar desconto de **10% nos meses que excedem a duração mínima** do plano, em `Plan.calculateTotalPrice(months)`.

**Alternativas:** desconto por `PlanType` com `if`/`else` (rejeitado por antecipar a refatoração da próxima etapa de forma errada); desconto no `EnrollmentService` (rejeitado por acoplar regra de precificação ao fluxo de matrícula).

**Justificativa:** o cálculo do preço total é uma informação derivada exclusivamente de atributos de `Plan` (`pricePerMonth`, `minimumDuration`) e do parâmetro `months`. Colocar a regra em `Plan.calculateTotalPrice` mantém a coesão e permite reutilização em qualquer contexto — inclusive prévias antes de matricular. A constante `DISCOUNT_RATE` é privada e declarada como `static final`, facilitando futuras substituições por subclasses de `Plan`.

**Impacto:** a fórmula é `base + (meses_excedentes × preço × 0.90)`. Quando `months <= minimumDuration`, não há desconto. O comportamento é independente de `PlanType`, o que evita o `if (type == ...)` que o enunciado expressamente desaconselha.

### 4.7 Cálculo e armazenamento da `endDate`

**Decisão:** calcular `endDate` no **construtor** de `Enrollment` como `startDate.plusMonths(durationMonths).minusDays(1)` e armazenar.

**Justificativa:** a data de término é uma informação consequente e imutável do contrato. Calcular no construtor garante que qualquer instância de `Enrollment` em memória tenha `endDate` consistente com `startDate` e `durationMonths`. A subtração de 1 dia é intencional: um plano iniciado em 01/03 por 3 meses termina em 31/05 (e não em 01/06). O uso de `LocalDate.plusMonths` foi a implementação escolhida, conforme sugestão do enunciado.

### 4.8 Pagamento inicial mínimo

**Decisão:** exigir um pagamento inicial com **valor positivo** (qualquer quantia > 0) no ato da matrícula.

**Alternativas:** exigir o equivalente a pelo menos uma parcela mensal; exigir percentual fixo do `totalPrice`.

**Justificativa:** percentuais ou parcelas mínimas são políticas de negócio de academias reais, mas variam entre unidades. A regra adotada garante a "efetivação" da matrícula (há pelo menos um pagamento associado) sem travar cenários legítimos. A validação fica no `FitManager.validateInitialPayment`, que também checa `PaymentType` nulo — sinalizando explicitamente que a regra pertence à orquestração, antes de criar qualquer objeto.

**Impacto:** o `Enrollment` é criado e o `Payment` inicial é adicionado em uma única chamada ao `EnrollmentService.enroll(...)`. A validação acontece **antes** da criação dos objetos, evitando estado intermediário.

### 4.9 Crédito e reembolso (pagamento excedente)

**Decisão:** **bloquear** pagamentos que excedam o saldo pendente (`amount > remainingBalance` → `OperationResult` com erro).

**Alternativas:** permitir crédito e apenas exibi-lo; aceitar pagamentos antecipados de meses futuros.

**Justificativa:** como `totalPrice` é fixado no contrato, um pagamento acima do saldo indica provavelmente um erro de digitação. Bloquear com mensagem explicativa é mais seguro para a integridade financeira nesta etapa; um modelo de "crédito" exigiria uma entidade separada ou campos adicionais em `Enrollment`.

**Impacto:** o método `EnrollmentService.registerPayment` exibe o saldo pendente na mensagem de erro quando o valor excede, orientando o usuário.

### 4.10 Taxas e data/motivo de cancelamento

**Decisão:** não aplicar taxa de cancelamento; **registrar apenas a data** (`cancelledAt`) e não o motivo.

**Justificativa:** uma política de taxas adicionaria regras dependentes do tempo de permanência e do `PlanType` — o que deve evoluir na etapa seguinte com herança e subclasses. Nesta etapa, o `cancel()` apenas altera o status para `CANCELLED` e registra a data, preservando o histórico financeiro intacto. O resumo financeiro do cancelamento é calculado dinamicamente pela interface usando `totalPrice`, `calculateTotalPaid()` (implícito em `calculateBalance`) e `calculateBalance()`.

**Impacto:** `Enrollment.cancel()` é idempotente — a transição de estado é verificada no `EnrollmentService.cancelEnrollment`, que rejeita dupla-cancelação.

### 4.11 Onde ficam as invariantes e as regras financeiras

**Decisão:**
- Regras que envolvem **um único objeto** → na classe de domínio (`Enrollment.calculateBalance`, `Plan.calculateTotalPrice`, `Student.calculateAge`).
- Regras que dependem de **coleções** → no serviço correspondente (`cpfExists`, `nameExists`, `hasActiveEnrollment`, rejeição de pagamento em matrícula cancelada).
- Regras que **coordenam múltiplos serviços** → no `FitManager` (remoção de aluno com matrícula ativa, validação do pagamento inicial antes de chamar `enroll`).

**Justificativa:** esta distribuição mantém cada camada com coesão máxima e acoplamento restrito à camada imediatamente inferior. O `FitManager` nunca implementa lógica que poderia ser resolvida por um único serviço, e os serviços nunca se comunicam entre si.

### 4.12 Situação financeira — estado ou cálculo?

**Decisão:** **cálculo derivado**. Não há atributo de situação financeira em `Enrollment`; `calculateBalance()` é chamado sob demanda.

**Justificativa:** um atributo separado precisaria ser atualizado a cada pagamento, criando risco de dessincronização com a lista de `payments`. Como a lista é a fonte da verdade, calcular sempre a partir dela elimina qualquer classe de bug relacionado a estado stale. O custo (iteração linear) é desprezível nas ordens de grandeza esperadas do sistema.

### 4.13 Uso de `null` como retorno

**Decisão:** convenção **mista justificada**.
- Métodos **públicos** de serviços sempre retornam `OperationResult` (nunca `null`).
- Métodos **internos/auxiliares** (`EnrollmentService.findByCode`) retornam `null` para ausência, pois são consumidos somente pelo próprio serviço que imediatamente verifica o retorno.

**Justificativa:** o contrato externo do sistema é uniforme (tudo é `OperationResult`), o que permite aos menus escreverem `if (result.isSuccess())` sem checar `null`. Internamente, `null` é aceitável quando o chamador é o próprio autor do método.

### 4.14 Inicialização com dados de teste

**Decisão:** `DataMock.populateDemo(fm)` é chamado no `FitManagerApp` quando `DEV_MODE = true`.

**Justificativa:** acelera o teste manual das listagens, cancelamentos e relatórios. A flag `DEV_MODE` é uma constante de classe e pode ser desativada com uma única alteração. O usuário é notificado visualmente quando o modo está ativo.

### 4.15 Seleção de `PlanType` / `PaymentType` na interface

**Decisão:** apresentar os valores do enum **numerados** ao usuário e mapear a escolha para o enum.

**Justificativa:** o parsing por texto livre é frágil (sensível a maiúsculas, acentos, traduções). A lista numerada é autoexplicativa, impossibilita typos e mantém a interface consistente entre `PlanMenu`/`EnrollmentMenu`. A mesma estratégia é reutilizada para ambos os enums.

### 4.16 Quem cria o `Payment` / atomicidade da matrícula

**Decisão:** o `EnrollmentService` cria o `Payment` (método auxiliar `buildPayment`) e chama `enrollment.addPayment(payment)`. `Enrollment` é receptora passiva.

**Justificativa:** a criação do `Payment` acontece em dois contextos (matrícula inicial e registro de pagamento avulso) e concentrá-la no serviço evita duplicação. Além disso, toda a validação precede a criação do `Enrollment` em `enroll(...)`: se algum parâmetro falha, nenhum dos dois objetos é criado. Isso garante atomicidade sem necessidade de lógica de *rollback*.

### 4.17 Onde verificar matrícula ativa

**Decisão:** **no `FitManager`**, antes de delegar ao serviço.

**Justificativa:** `hasActiveEnrollment` é um método público do `EnrollmentService` que pertence naturalmente ao serviço (ele detém a coleção). O `FitManager` o consulta em dois fluxos distintos (matrícula nova e remoção de aluno), evitando duplicação da regra dentro do serviço. Manter a verificação no orquestrador também simplifica `EnrollmentService.enroll`, que não precisa conhecer regras sobre "ter ou não ter matrícula ativa".

### 4.18 Tratamento de entrada numérica

**Decisão:** classe estática `InputParser` com parsing **sem `try/catch`**, percorrendo caracteres manualmente. Retorna valores sentinela (`Integer.MIN_VALUE`, `Double.NaN`) para entradas inválidas.

**Alternativas:** `try/catch` em `NumberFormatException` a cada uso.

**Justificativa:** o parsing manual torna a validação explícita e permite um ponto único de controle — todos os menus e `UserInterface.getIntInput/getDoubleInput` delegam a essa classe. Aceita vírgula como separador decimal (convertida para ponto). O valor sentinela é detectado nos menus, que exibem mensagem amigável e permanecem no loop.

### 4.19 Organização, filtragem e formatação das listagens

**Decisão:**
- **Filtragem** → nos serviços (`listActive`, `listWithPendingBalance`, `listHistoryByStudent`). Os menus nunca filtram coleções.
- **Ordenação** → não implementada nesta etapa, pois os dados de demonstração já são inseridos em ordem lógica; quando necessário, ficará também no serviço.
- **Formatação** → no `toString()` de cada classe de domínio, usado pelos menus ao montar a exibição.

**Justificativa:** cada classe conhece seus próprios campos e é a melhor candidata a apresentá-los. Os menus iteram nas coleções e concatenam `toString()`, o que elimina duplicação entre tipos de listagem que compartilham a mesma entidade.

### 4.20 Histórico de preços

**Decisão:** não armazenar histórico de preços em `Plan`. Apenas o valor atual é mantido; o preço efetivo de cada contrato vive em `Enrollment.totalPrice`.

**Justificativa:** `totalPrice` já garante a imutabilidade histórica exigida pelo enunciado ("alterações no preço não afetam matrículas registradas"). Um histórico de preços completo exigiria outra entidade (`PlanPriceHistory`) — complexidade desproporcional ao valor agregado nesta etapa.

### 4.21 Formatação de valores monetários e datas (pacote `util`)

**Decisão:** centralizar toda formatação de valores em BRL e de datas/horas em duas classes estáticas — `util.CurrencyFormatter` e `util.DateFormatter` — baseadas em `java.text.NumberFormat` e `java.time.format.DateTimeFormatter` com `Locale("pt","BR")`.

**Motivação:** a versão inicial do projeto usava `String.format("%.2f", valor)` para valores monetários e `String.format("%02d/%02d/%04d", ...)` para datas, replicados em 8+ lugares diferentes. Esse padrão apresentava dois problemas: (1) `%.2f` respeita o Locale do sistema, mas não acrescenta o separador de milhar — o `getSystemStatistics()`, por exemplo, imprimia `R$ 2596,50` em vez do esperado `R$ 2.596,50`; (2) a construção manual da data via `getDayOfMonth()/getMonthValue()/getYear()` era verbosa e propensa a inconsistências caso alguém esquecesse o `%02d`.

**Alternativas:** manter `String.format` em cada ponto; usar `DateTimeFormatter.ofPattern("dd/MM/yyyy")` inline em cada `toString()`.

**Justificativa:** `NumberFormat.getNumberInstance(new Locale("pt","BR"))` já aplica ponto como separador de milhar e vírgula como decimal. `DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("pt","BR"))` descreve declarativamente a saída desejada. Encapsular ambos em classes utilitárias garante:
- **Saída consistente em todo o sistema** — qualquer ajuste de padrão (ex.: trocar para moeda com código `BRL`) ocorre em um único arquivo.
- **Ponto único de parse** — o método `DateFormatter.parseDate(String)` substitui as chamadas repetidas a `LocalDate.parse(..., DateTimeFormatter.ofPattern("dd/MM/yyyy"))` em `StudentService` e `FitManager`.
- **API mais legível nos `toString()`** — `DateFormatter.format(startDate)` em vez de três chamadas aninhadas.

`CurrencyFormatter` expõe `format(double)`, `formatCurrency(double)` e `formatNumber(double)`; `DateFormatter` expõe sobrecargas de `format` para `LocalDate`, `LocalTime` e `LocalDateTime`, além de constantes públicas `DATE_PATTERN`, `TIME_PATTERN`, `DATE_TIME_PATTERN` e os respectivos `DateTimeFormatter`.

**Impacto:** todas as classes de domínio que possuem `toString()` com valores monetários ou datas (`Enrollment`, `Student`, `Payment`, `Plan`) passaram a delegar aos utilitários, assim como os menus `EnrollmentMenu` e `ReportsMenu` e os serviços/orquestrador que faziam parse. A pasta `util` foi criada para abrigar estas e futuras classes utilitárias puramente transversais ao domínio.

### 4.22 Comentários e evolução futura

**Política de comentários:** Javadoc para descrever intenção e contexto (por que a decisão foi tomada), nunca para descrever o óbvio do código.

**Pontos de extensão preservados:**
- `PlanType` → futuras subclasses de `Plan`; por isso `calculateTotalPrice` não contém `if (type == ...)`.
- `PaymentType` → futuras subclasses de `Payment` com comportamentos específicos.
- `UserInterface` → futura conversão em interface Java; por isso os menus só conhecem o tipo `UserInterface` (nunca uma implementação específica).
- `OperationResult` → campo `data: Object` preparado para se tornar genérico `T`.

---

## 5. Regras de negócio implementadas

| # | Regra | Local no código |
|---|---|---|
| 1 | CPF único em todo o sistema | `StudentService.cpfExists` (chamado em `registerStudent`) |
| 2 | Todos os campos do aluno são obrigatórios | `StudentService.validateRequiredFields` |
| 3 | Validação completa do CPF (formato + dígito verificador) | `Student.validateCpf` (estático) |
| 4 | Data de nascimento não pode ser futura | `StudentService.registerStudent` |
| 5 | Aluno com matrícula ativa não pode ser removido | `FitManager.removeStudent` |
| 6 | Remoção preserva histórico (inativação via `deactivate`) | `Student.deactivate` |
| 7 | Nome do plano único | `PlanService.nameExists` |
| 8 | Duração mínima > 0 e preço/mês > 0 | `PlanService.registerPlan` |
| 9 | Atualização de preço não afeta matrículas existentes | `Enrollment` armazena `totalPrice` no construtor |
| 10 | Aluno e plano devem existir para matricular | `FitManager.enrollStudent` |
| 11 | Um aluno não pode ter duas matrículas ativas | `FitManager.enrollStudent` via `hasActiveEnrollment` |
| 12 | Duração ≥ duração mínima do plano | `EnrollmentService.validateEnrollmentParams` |
| 13 | `totalPrice` fixado no ato da matrícula (imutável após) | `Enrollment` construtor |
| 14 | Matrícula só efetivada com pagamento inicial > 0 | `FitManager.validateInitialPayment` |
| 15 | Pagamento em matrícula cancelada é bloqueado | `EnrollmentService.registerPayment` |
| 16 | Valor de pagamento deve ser positivo | `EnrollmentService.validatePaymentParams` |
| 17 | Pagamento não pode exceder saldo pendente | `EnrollmentService.registerPayment` |
| 18 | `PlanType`/`PaymentType` obrigatórios e válidos | seleção numerada nos menus |
| 19 | Cancelamento só sobre matrícula `ACTIVE` | `EnrollmentService.cancelEnrollment` |
| 20 | Cancelamento é irreversível | `Enrollment.cancel` (transição unidirecional) |
| 21 | Histórico de pagamentos preservado após cancelamento | `Enrollment` mantém lista, `cancel()` só altera status |
| 22 | `calculateBalance()` derivado dinamicamente | `Enrollment.calculateBalance` |
| 23 | Desconto de 10% sobre meses excedentes à duração mínima | `Plan.calculateTotalPrice` |
| 24 | Entradas inválidas não encerram o programa | `InputParser` + valores sentinela |
| 25 | `nextCode` estático garante unicidade dos códigos de matrícula | `Enrollment.nextCode` |

**Regras opcionais não implementadas (com justificativa):**
- Bloqueio de remoção por pendência financeira em matrículas canceladas — descartado para evitar retenção indefinida de alunos inativos; a regra obrigatória (matrícula ativa) é suficiente para preservar consistência.
- Taxas de cancelamento — postergadas para a etapa seguinte, quando subclasses de `Plan` serão o local natural para políticas específicas por tipo.
- Registro de motivo do cancelamento — apenas a data (`cancelledAt`) foi registrada; o motivo pode ser adicionado depois sem impacto em outros componentes.

---

## 6. Funcionalidades extras

### 6.1 Relatório de estatísticas gerais

Método `FitManager.getSystemStatistics()` consolida informações de múltiplas entidades: total de alunos, planos, matrículas, matrículas ativas e saldo pendente total. Nenhuma classe nova foi criada — o método apenas itera sobre as coleções existentes via `listAllStudents`, `listAllPlans` e `listAllEnrollments`.

### 6.2 Modo de desenvolvimento (`DataMock`)

A classe `mocks.DataMock` popula o sistema com cenários representativos: alunos ativos e inativos, matrículas ativas, canceladas, histórico múltiplo por aluno e aluno inativo com histórico. Controlado pela flag `DEV_MODE` em `FitManagerApp`, facilita o teste manual dos oito fluxos críticos sem precisar cadastrar dados manualmente a cada execução.

### 6.3 Desconto progressivo em `Plan.calculateTotalPrice`

Descrito em 4.6. Implementa uma regra de precificação realista sem acoplar-se ao `PlanType`, preservando o ponto de extensão para subclasses.

### 6.4 `InputParser` como utilitário dedicado

Classe estática com parsing manual (sem `try/catch`) que unifica o tratamento de entradas numéricas de todos os menus. Contribui para a robustez exigida pelo requisito 7 (tratamento de erros) e mantém a responsabilidade de conversão isolada da interface.

### 6.5 Tela com rolagem (`showScrollableMessage`)

Adicionada à `UserInterface` para listagens extensas (histórico de matrículas, lista completa de alunos). Mantém a restrição arquitetural — toda I/O continua concentrada na mesma classe.

### 6.6 Pacote `util` — `CurrencyFormatter` e `DateFormatter`

Classes estáticas que centralizam a formatação de valores monetários em reais (`NumberFormat` com `Locale("pt","BR")`) e de datas/horas (`DateTimeFormatter` para `LocalDate`, `LocalTime` e `LocalDateTime`). Substituem as chamadas repetidas a `String.format("%.2f", ...)` e `String.format("%02d/%02d/%04d", ...)` espalhadas pelo projeto, garantindo que valores como `2596.5` sejam sempre exibidos como `R$ 2.596,50` (com separador de milhar) e que datas sigam sempre o padrão `dd/MM/yyyy`. Descritas em detalhes em 4.21.

---

## 7. Dificuldades e aprendizados

**Principais dificuldades:**

- **Delimitar fronteiras entre camadas.** O impulso inicial era colocar validações nos menus porque "estava mais perto do usuário". Algumas validações foram migradas para os serviços durante revisões de pull request, e a regra "menu coleta e exibe, serviço decide" passou a ser explicitamente verificada nas revisões.
- **Coordenação entre serviços.** Descobrir que o `FitManager` é o único ponto onde `StudentService` e `EnrollmentService` se cruzam exigiu uma leitura cuidadosa do enunciado. Depois que ficou claro, o método `removeStudent` passou a ser o modelo mental para outras operações coordenadas.
- **Parsing robusto sem `try/catch`.** Decidir por uma classe utilitária com valores sentinela (em vez de exceções) levou a código mais explícito, mas exigiu disciplina para que **todos** os menus checassem o sentinela antes de usar o valor.
- **Manter `Enrollment` coeso.** Com pagamento inicial, cancelamento, status e saldo, havia tentação de crescer a classe. A solução foi mover regras que envolvem a coleção (ex.: dupla-cancelação, pagamento em cancelada) para o `EnrollmentService`.

**Aprendizados:**

- **`OperationResult` é uma escolha arquitetural, não cosmética.** Uniformizar retornos elimina dezenas de decisões pontuais nos menus.
- **`static` com propósito.** `nextCode`, `cleanCpf` e `validateCpf` são usos intencionais de `static` — atributos/métodos que de fato não dependem de instância. Evitar `static` como "atalho" para coisas que deveriam ser de instância foi um cuidado constante.
- **Separar I/O é libertador.** Com toda a interação concentrada em `UserInterface`, uma eventual troca para terminal ou interface gráfica completa fica restrita a um único arquivo.
- **Preparar para evolução.** Evitar `if (type == ...)` em `Plan.calculateTotalPrice` e preservar `UserInterface` como tipo referenciado pelos menus são decisões pequenas agora que poupam horas de refatoração na próxima etapa.

**O que faríamos diferente:**

- Criar os testes manuais desde o início do desenvolvimento (não só no final), via `DataMock` incremental por funcionalidade.
- Estabelecer antes da primeira branch a convenção de retorno (`OperationResult` vs `null` interno) — algumas revisões foram gastas só padronizando esse ponto.
- Definir logo no começo a estratégia de formatação (via `toString`), evitando que menus produzissem formatações distintas para a mesma entidade.
