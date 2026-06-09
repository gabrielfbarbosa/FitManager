# Relatório — FitManager

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

**Decisão:** classe estática `InputParser` com parsing **sem `try/catch`**, percorrendo caracteres manualmente. Retorna valores sentinela constantes definidos na própria classe (`InputParser.INVALID_INT`, representando `-1`, e `InputParser.INVALID_DOUBLE`, representando `-1.0`) para entradas inválidas.

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

`CurrencyFormatter` expõe `formatCurrency(double)`; `DateFormatter` expõe sobrecargas de `format` para `LocalDate`, além de constantes públicas `DATE_PATTERN`, `TIME_PATTERN`, `DATE_TIME_PATTERN` e os respectivos `DateTimeFormatter`.

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

Classes estáticas que centralizam a formatação de valores monetários em reais (`NumberFormat` com `Locale("pt","BR")`) e de datas (`DateTimeFormatter` para `LocalDate`). Substituem as chamadas repetidas a `String.format("%.2f", ...)` e `String.format("%02d/%02d/%04d", ...)` espalhadas pelo projeto, garantindo que valores como `2596.5` sejam sempre exibidos como `R$ 2.596,50` (com separador de milhar) e que datas sigam sempre o padrão `dd/MM/yyyy`. Descritas em detalhes em 4.21.

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

---
---

# Relatório — FitManager (Etapa 2)

## 1. Introdução da Etapa 2

Nesta segunda etapa, o sistema FitManager foi refatorado para incorporar os conceitos centrais de Programação Orientada a Objetos: classes abstratas, herança, polimorfismo e interfaces Java. As três transformações principais foram: (1) conversão de `Plan` em classe abstrata com quatro subclasses especializadas (`MonthlyPlan`, `QuarterlyPlan`, `SemiAnnualPlan`, `AnnualPlan`), cada uma com regras próprias de desconto e cancelamento; (2) conversão de `Payment` em classe abstrata com quatro subclasses (`PixPayment`, `CreditCardPayment`, `DebitCardPayment`, `CashPayment`), cada uma com atributos exclusivos e comportamentos diferenciados; (3) conversão de `UserInterface` de classe concreta para interface Java com duas implementações (`TerminalUI` e `JOptionPaneUI`), selecionáveis na inicialização do sistema.

Além das transformações obrigatórias, foram implementadas funcionalidades extras que demonstram o uso dos conceitos desta etapa: a interface `Summarizable` como contrato polimórfico para exibição compacta de entidades em listagens, e um sistema de filtros polimórficos baseado na interface `EnrollmentFilter` com cinco implementações, que amplia o menu de relatórios sem duplicação de código. Todas as funcionalidades da etapa anterior foram preservadas após a refatoração — nenhum fluxo crítico foi quebrado.

---

## 2. Diagrama de classes atualizado

O diagrama de classes atualizado encontra-se no arquivo `diagram.png` na raiz do repositório. As principais alterações em relação à etapa anterior:

- `Plan` está identificada como classe abstrata (`<<abstract>>`) com os métodos abstratos `calculateTotalPrice(months: int): double` e `getCancellationFee(enrollment: Enrollment): double`. As quatro subclasses — `MonthlyPlan`, `QuarterlyPlan`, `SemiAnnualPlan` e `AnnualPlan` — estão conectadas à superclasse com setas de herança (linha sólida, triângulo fechado).
- `Payment` está identificada como classe abstrata com os métodos abstratos `getProcessingFee(): double` e `getPaymentSummary(): String`. As quatro subclasses — `PixPayment`, `CreditCardPayment`, `DebitCardPayment` e `CashPayment` — estão conectadas com setas de herança. Os atributos específicos de cada subclasse (`pixKey`, `installments`, `cardLastDigits`, `amountReceived`) aparecem apenas nas subclasses correspondentes.
- `UserInterface` está identificada como interface (`<<interface>>`) com cinco assinaturas de método. `TerminalUI` e `JOptionPaneUI` estão conectadas com setas de implementação (linha tracejada, triângulo fechado).
- A interface `Summarizable` aparece com o método `getSummary(): String`, implementada por `Student`, `Plan`, `Enrollment` e `Payment` (linhas tracejadas).
- A interface `EnrollmentFilter` aparece com os métodos `matches(enrollment: Enrollment): boolean` e `getDescription(): String`, com cinco implementações: `ActiveEnrollmentFilter`, `CancelledEnrollmentFilter`, `PendingBalanceFilter`, `ByPlanTypeFilter` e `ExpiredEnrollmentFilter`.
- Os pacotes de domínio foram organizados em subpacotes: `domain.model.plans` para a hierarquia de planos, `domain.model.payments` para a hierarquia de pagamentos, `domain.model.filters` para os filtros polimórficos, e `domain.model.enums` para os enums.

O diagrama foi utilizado como ferramenta ativa de projeto desde o início da etapa — as hierarquias foram desenhadas no diagrama antes de serem implementadas no código, e o diagrama foi atualizado a cada transformação significativa. A versão anterior do diagrama foi preservada como `diagram-stage1.png`.

---

## 3. Decisões de projeto da Etapa 2

### 3.1 Hierarquia de Plan — o que pertence à superclasse

**Decisão:** os atributos `name`, `description`, `type` (PlanType), `minimumDuration` e `pricePerMonth` pertencem à superclasse abstrata `Plan`. Os métodos `calculateTotalPrice(int months)` e `getCancellationFee(Enrollment enrollment)` são declarados como abstratos. Métodos concretos como `getTypeName()`, `getSummary()` e `toString()` residem na superclasse, pois operam exclusivamente sobre atributos comuns.

**Alternativas consideradas:** colocar o `DISCOUNT_RATE` como atributo na superclasse (rejeitado — cada subclasse define sua própria constante de desconto); criar um método concreto `applyDiscount` na superclasse que as subclasses usariam (rejeitado — embora elegante, a lógica de cada subclasse é suficientemente simples e distinta para justificar implementações independentes nesta etapa).

**Justificativa:** a análise seguiu a regra: "atributos iguais em todas as subclasses pertencem à superclasse; comportamentos que variam pertencem às subclasses via métodos abstratos". Nenhuma subclasse herda atributos que não utiliza, e nenhuma subclasse precisa sobrescrever um método para retornar um valor fixo artificialmente. A constante `DISCOUNT_RATE` é declarada como `private static final` em cada subclasse que a possui (`QuarterlyPlan`: 0.05, `SemiAnnualPlan`: 0.10, `AnnualPlan`: 0.15), mantendo cada taxa encapsulada na subclasse que a define.

**Impacto:** adicionar um quinto tipo de plano no futuro requer apenas criar uma nova subclasse com as duas implementações abstratas — nenhuma outra classe do sistema precisa ser alterada (exceto o `PlanService` para a instanciação e o `PlanType` para o novo valor do enum).

### 3.2 Instanciação da subclasse correta no PlanService

**Decisão:** a instanciação das subclasses de `Plan` é feita dentro do `PlanService`, em um método privado `createPlanByType(name, description, type, minimumDuration, pricePerMonth)` que usa um `switch` sobre o `PlanType`.

**Alternativas consideradas:** criar uma classe `PlanFactory` separada (rejeitado — para quatro subclasses estáveis, um factory separado adiciona uma classe sem ganho proporcional); colocar a instanciação no `FitManager` (rejeitado — o FitManager é orquestrador, não criador de objetos de domínio); colocar no menu (rejeitado — viola a separação de camadas).

**Justificativa:** o `PlanService` é o único ponto do sistema que conhece as subclasses concretas de `Plan`. Todas as demais classes — `FitManager`, `EnrollmentService`, menus — referenciam apenas o tipo `Plan`. O `switch` no serviço é um uso legítimo de condicional por tipo, pois ocorre no momento da instanciação (o objeto ainda não existe, então o polimorfismo não pode atuar). Se um quinto tipo for adicionado, apenas o `PlanService` e o enum `PlanType` precisam ser modificados — dois arquivos.

### 3.3 O enum PlanType ainda tem papel no sistema?

**Decisão:** manter o `PlanType` como enum no sistema.

**Alternativas consideradas:** removê-lo inteiramente e usar `instanceof` ou nomes de classe para identificar tipos (rejeitado — `instanceof` é mais invasivo e menos legível).

**Justificativa:** o `PlanType` continua útil em três contextos que não são cobertos pelo polimorfismo: (1) no `PlanMenu`, para apresentar as opções numeradas ao usuário durante o cadastro de planos — o enum fornece os labels amigáveis ("Mensal", "Trimestral", etc.) sem precisar instanciar objetos; (2) no `PlanService`, para decidir qual subclasse instanciar; (3) no `ByPlanTypeFilter`, para filtrar matrículas por tipo de plano nos relatórios. Em nenhum desses casos o enum substitui o polimorfismo — ele serve exclusivamente a cenários onde o tipo concreto ainda não existe (instanciação), onde a apresentação ao usuário exige rótulos legíveis (menus), ou onde a filtragem por categoria é necessária (relatórios). O `PlanType` é armazenado como atributo `type` em `Plan` para facilitar esses usos sem recorrer a `instanceof`.

### 3.4 Regras de cálculo do preço total por tipo de plano

**Decisão:** cada subclasse implementa `calculateTotalPrice(int months)` com sua própria regra de desconto, aplicada sobre o valor bruto total (`pricePerMonth × months`).

**Regras implementadas:**

| Tipo | Desconto | Condição |
|------|----------|----------|
| `MonthlyPlan` | 0% | Nunca aplica desconto |
| `QuarterlyPlan` | 5% | Quando `months > minimumDuration` |
| `SemiAnnualPlan` | 10% | Quando `months > minimumDuration` |
| `AnnualPlan` | 15% | Quando `months > minimumDuration` |

**Desconto sobre o valor total, não sobre o preço mensal:** a fórmula usada é `(pricePerMonth × months) × (1 − discountRate)`. Essa abordagem facilita a evolução para cenários mais complexos, como descontos progressivos por faixa de duração, pois o valor bruto total é calculado primeiro e o desconto aplicado depois. A estrutura atual permite, por exemplo, que uma subclasse futura aplique 5% nos primeiros 6 meses e 10% nos meses seguintes, bastando sobrescrever o `calculateTotalPrice` com lógica mais elaborada — sem alterar a superclasse nem outras subclasses.

**Impacto na etapa anterior:** na etapa 1, `Plan` era uma classe concreta com um único método `calculateTotalPrice` que aplicava 10% de desconto sobre meses excedentes da duração mínima, independentemente do tipo. Com a hierarquia, essa regra genérica foi substituída por quatro regras específicas, cada uma encapsulada na subclasse correspondente. O `EnrollmentService` continua chamando `plan.calculateTotalPrice(months)` sem nenhuma alteração — o polimorfismo garante que a subclasse correta responde.

### 3.5 Taxa de cancelamento: semântica e implementação

**Decisão:** a taxa de cancelamento é um valor **informativo** calculado e exibido ao usuário no momento do cancelamento, mas que **não afeta** o `calculateBalance()` da matrícula. A taxa não é adicionada ao saldo pendente nem subtraída de pagamentos já realizados.

**Alternativas consideradas:** somar a taxa ao saldo pendente (rejeitado — mistura o conceito de "dívida contratual" com "penalidade por rescisão", dificultando a conciliação financeira); subtrair a taxa do total já pago (rejeitado — alteraria retroativamente o valor de pagamentos já realizados).

**Justificativa:** a taxa de cancelamento é uma consequência do cancelamento, não uma parcela do contrato original. No domínio de uma academia real, essa taxa seria cobrada separadamente — como uma multa rescisória. Implementá-la como valor informativo mantém o modelo financeiro simples e coerente: `calculateBalance()` reflete apenas a relação entre o `totalPrice` contratado e os pagamentos realizados.

**Exemplo numérico:** matrícula em `AnnualPlan` por 12 meses, `totalPrice = R$ 1.020,00` (com desconto de 15% sobre o bruto de R$ 1.200,00). Aluno pagou `R$ 500,00`. Cancelamento em 4 meses (antes da metade de 6 meses). Taxa de cancelamento: 20% de R$ 1.020,00 = R$ 204,00. O resumo exibe: total contratado R$ 1.020,00, total pago R$ 500,00, saldo pendente R$ 520,00, taxa de cancelamento R$ 204,00. O saldo pendente permanece R$ 520,00 — a taxa é informada separadamente.

**Regras por tipo:**

| Tipo | Taxa de cancelamento |
|------|---------------------|
| `MonthlyPlan` | 0% — sem taxa |
| `QuarterlyPlan` | 0% — sem taxa |
| `SemiAnnualPlan` | 0% — sem taxa |
| `AnnualPlan` | 20% do `totalPrice` se cancelado antes da metade do período; 0% após (inclusive) |

### 3.6 Por que `getCancellationFee` recebe o Enrollment como parâmetro

**Decisão:** o método recebe o objeto `Enrollment` completo, e não parâmetros isolados.

**Alternativas consideradas:** receber apenas `startDate`, `durationMonths` e `totalPrice` (rejeitado — engessa a assinatura e dificulta a evolução para regras mais complexas que possam depender de outros atributos da matrícula).

**Justificativa:** o `AnnualPlan` precisa de `startDate`, `durationMonths` e `totalPrice` para calcular a taxa. Receber o `Enrollment` completo simplifica a assinatura e permite que subclasses futuras acessem informações adicionais (como a lista de pagamentos ou o status) sem alterar o contrato da superclasse. O acoplamento introduzido é aceitável: `Plan` já possui uma relação semântica com `Enrollment` no domínio (um plano é contratado por uma matrícula), e o acoplamento é unidirecional — `Plan` conhece `Enrollment`, mas não o contrário.

### 3.7 Metade do período: definição e caso-limite

**Decisão:** a metade do período é calculada como `startDate.plusMonths(durationMonths / 2)`. Se o cancelamento ocorre **exatamente** na data que marca a metade, a taxa é **isenta** (a condição é `today.isBefore(halfwayDate)`, não `isBefore` ou `isEqual`).

**Justificativa:** interpretar "metade cumprida" como "na data da metade ou depois" é a abordagem mais favorável ao aluno, que é o padrão esperado em contratos de adesão. Usar `isBefore` (estritamente antes) para aplicar a taxa e isentar qualquer dia a partir da metade (inclusive) é a regra mais clara e defensável.

### 3.8 Taxa de cancelamento e saldo pendente são independentes

**Decisão:** a taxa de cancelamento e o saldo pendente são tratados como informações independentes, apresentadas separadamente no resumo do cancelamento.

**Justificativa:** são conceitos distintos no domínio: o saldo pendente é a diferença entre o contrato e os pagamentos realizados; a taxa é uma penalidade pelo cancelamento antecipado. Somá-los em um único valor obscurece a informação para o usuário. O resumo financeiro exibe ambos de forma clara: valor total contratado, total já pago, saldo pendente (ou crédito), e taxa de cancelamento quando aplicável. A taxa é aplicada mesmo quando o aluno está inadimplente — ela é uma consequência do cancelamento, não da situação financeira.

### 3.9 Ordem das operações no cancelamento: calcular taxa antes de cancelar

**Decisão:** a taxa de cancelamento é calculada **antes** de chamar `enrollment.cancel()`.

**Alternativas consideradas:** calcular a taxa dentro de `Enrollment.cancel()` (rejeitado — expande a responsabilidade de `cancel()`, que na etapa anterior era simples: alterar status para `CANCELLED` e registrar a data).

**Justificativa:** `cancel()` tem uma responsabilidade clara e coesa: efetuar a transição de estado. O cálculo da taxa envolve o `Plan` associado à matrícula, e essa lógica pertence ao serviço que coordena a operação. Manter o cálculo no `EnrollmentService.cancelEnrollment()` preserva a coesão de `Enrollment.cancel()` e evita que o método de domínio precise retornar valores que não são do seu escopo. O acoplamento do serviço com `enrollment.getPlan().getCancellationFee(enrollment)` é aceitável — o serviço já gerencia a relação entre matrículas e planos.

### 3.10 O que exibir quando a taxa é zero

**Decisão:** quando a taxa de cancelamento é zero, o resumo **omite** qualquer menção à taxa. Não é feita distinção entre "plano que não aplica taxa" e "plano que aplicaria taxa mas a condição de isenção foi atingida".

**Alternativas consideradas:** informar ao usuário quando a taxa foi isenta e o motivo (considerado mas não implementado — exigiria que `getCancellationFee` retornasse uma estrutura mais rica que apenas um `double`, o que complicaria a interface sem ganho proporcional para o MVP).

**Justificativa:** para o escopo atual, a informação mais importante para o usuário é: "há taxa?" e "quanto?". Se não há, omitir simplifica a interface. Em uma evolução futura, `getCancellationFee` poderia retornar um `CancellationResult` com valor e motivo, preservando o polimorfismo — mas essa complexidade foi deliberadamente adiada.

### 3.11 Hierarquia de Payment — o que pertence à superclasse

**Decisão:** os atributos `code`, `amount`, `paymentDate` (LocalDateTime), `paymentType` (PaymentType) e `description` pertencem à superclasse abstrata `Payment`. Os métodos `getProcessingFee()` e `getPaymentSummary()` são declarados como abstratos. Métodos concretos como `getEffectiveAmount()`, `getTypeName()`, `getSummary()` e `toString()` residem na superclasse.

**Atributos específicos por subclasse:**

| Subclasse | Atributos exclusivos |
|-----------|---------------------|
| `PixPayment` | `pixKey: String` |
| `CreditCardPayment` | `installments: int`, `cardLastDigits: String` |
| `DebitCardPayment` | `cardLastDigits: String` |
| `CashPayment` | `amountReceived: double` |

Nenhum desses atributos foi colocado na superclasse — eles fazem sentido apenas para suas respectivas subclasses. `CashPayment` possui adicionalmente um método exclusivo `getChange()` que retorna o troco (`amountReceived - amount`).

### 3.12 Taxa de processamento: absorção pela academia

**Decisão:** a taxa de processamento é **absorvida pela academia**. O aluno paga o valor nominal; o valor efetivamente creditado é menor.

**Alternativas consideradas:** repassar a taxa ao aluno (o pagamento registrado seria `amount + fee`) — rejeitado por ser menos transparente para o aluno e por complicar o fluxo de coleta no menu (o usuário informaria um valor e o sistema registraria outro).

**Justificativa:** com a absorção, o fluxo é intuitivo: o aluno paga R$ 100,00, o sistema registra R$ 100,00 como pagamento, e a academia absorve a taxa. O método `getEffectiveAmount()` retorna `amount - getProcessingFee()` para fins de relatório interno. A taxa de processamento **não afeta** o `calculateBalance()` da matrícula — o saldo é calculado com base no `amount` nominal, não no efetivo. Essa decisão simplifica o modelo financeiro.

**Exemplo numérico:** pagamento de R$ 100,00 via cartão de crédito (taxa de 2,5%). Valor registrado: R$ 100,00. Taxa de processamento: R$ 2,50. Valor efetivamente creditado à academia: R$ 97,50. O saldo da matrícula é reduzido em R$ 100,00 (não em R$ 97,50).

**Taxas por tipo:**

| Tipo | Taxa de processamento |
|------|----------------------|
| `PixPayment` | 0% |
| `CreditCardPayment` | 2,5% sobre o valor nominal |
| `DebitCardPayment` | 0% |
| `CashPayment` | 0% |

### 3.13 `getPaymentSummary()` versus `toString()`

**Decisão:** manter ambos, com propósitos distintos. `getPaymentSummary()` é o método abstrato do contrato polimórfico, voltado à exibição ao usuário com informações específicas de cada tipo. `toString()` é o método genérico herdado de `Object`, sobrescrito na superclasse `Payment` com os dados comuns (código, valor, data, tipo, descrição), voltado à depuração e inspeção.

**Justificativa:** `toString()` serve como representação técnica do objeto — útil em logs e debug. `getPaymentSummary()` é uma informação de domínio voltada ao usuário, com formatação elaborada e detalhes específicos do tipo (parcelas, chave PIX, troco). Separar os dois evita que a representação de depuração fique poluída com lógica de apresentação, e vice-versa. O `getPaymentSummary()` é chamado polimorficamente nos menus e relatórios; o `toString()` pode ser usado em contextos de desenvolvimento sem interferir na interface.

### 3.14 Troco do CashPayment: domínio ou interface?

**Decisão:** o troco é incluído no `getPaymentSummary()` do `CashPayment`, sendo responsabilidade do domínio.

**Justificativa:** o troco é uma informação derivada exclusivamente de atributos do próprio `CashPayment` (`amountReceived - amount`), o que o posiciona como responsabilidade da classe de domínio. Incluí-lo no `getPaymentSummary()` mantém o princípio de que cada subclasse é responsável por apresentar suas informações relevantes — o menu não precisa saber que `CashPayment` tem troco, pois o polimorfismo cuida disso. O método exclusivo `getChange()` permanece público para cenários onde o menu ou relatório precise do valor numérico isolado.

### 3.15 O enum PaymentType ainda tem papel no sistema?

**Decisão:** manter o `PaymentType`, pelo mesmo raciocínio aplicado ao `PlanType` (seção 3.3).

**Justificativa:** o `PaymentType` é usado: (1) no `EnrollmentMenu` para apresentar as opções de pagamento ao usuário; (2) no `EnrollmentService.createPaymentByType()` para instanciar a subclasse correta; (3) no `Payment.getSummary()` e `toString()` para exibir o label amigável do tipo. Em nenhum caso ele substitui o polimorfismo — serve a contextos de apresentação e instanciação.

### 3.16 Instanciação da subclasse correta no EnrollmentService

**Decisão:** a instanciação das subclasses de `Payment` é feita no `EnrollmentService`, em um método privado `createPaymentByType(amount, paymentDate, paymentType, description, paymentData)` com `switch` sobre `PaymentType`.

**Justificativa:** análoga à decisão 3.2 para `Plan`. O `EnrollmentService` é o único ponto do sistema que conhece as subclasses concretas de `Payment`. O método é reutilizado tanto no fluxo de matrícula (`enroll()`) quanto no registro de pagamento avulso (`registerPayment()`), evitando duplicação. Todas as demais classes referenciam apenas o tipo `Payment`.

### 3.17 Transporte dos dados de pagamento do menu ao serviço

**Decisão:** usar `String[] paymentData` para transportar os dados adicionais específicos de cada tipo de pagamento.

**Alternativas consideradas:** criar uma classe intermediária `PaymentData` com campos para todos os tipos (rejeitado — adiciona uma classe que não pertence ao domínio e cujos campos seriam parcialmente nulos em cada uso); usar parâmetros individuais no método (rejeitado — tornaria a assinatura muito longa e frágil).

**Justificativa:** o array de strings é simples e suficiente para o estágio atual. Cada tipo define convencionalmente a ordem dos seus dados: PIX usa `[pixKey]`; cartão de crédito usa `[installments, cardLastDigits]`; débito usa `[cardLastDigits]`; dinheiro usa `[amountReceived]`. O `createPaymentByType` interpreta o array conforme o tipo. Essa abordagem evita a criação de classes adicionais e mantém o fluxo direto. Em uma evolução futura, com persistência ou validações mais complexas, um objeto intermediário ou o padrão Builder poderia substituir o array — mas a complexidade atual não justifica.

### 3.18 Validações específicas de subclasse: menu ou serviço?

**Decisão:** centralizar todas as validações de pagamento no `EnrollmentService.validatePaymentParams()`, incluindo a regra `amountReceived >= amount` do `CashPayment`.

**Alternativas consideradas:** validar no construtor do `CashPayment` lançando exceção (rejeitado — na etapa anterior o padrão adotado foi validar nos serviços e retornar `OperationResult` sem exceções; manter consistência é mais importante que purismo); validar no menu (rejeitado — coloca lógica de negócio na camada de apresentação).

**Justificativa:** o `validatePaymentParams()` já existia para validações genéricas (valor positivo, tipo não nulo). Adicionar a validação específica do `CashPayment` ali mantém um único ponto de validação antes da criação de qualquer objeto, consistente com o padrão estabelecido na etapa 1. A validação ocorre antes de `createPaymentByType()` — se inválido, nenhum objeto é criado.

### 3.19 Interface UserInterface: interface vs. classe abstrata

**Decisão:** converter `UserInterface` em **interface Java** pura (sem atributos de instância, sem corpo de método).

**Alternativas consideradas:** usar classe abstrata que centralizasse comportamentos comuns às duas implementações.

**Justificativa:** após análise, não foi identificado nenhum comportamento genuinamente compartilhado entre `TerminalUI` e `JOptionPaneUI`. As duas implementações são radicalmente diferentes em mecanismo: uma usa `Scanner`/`System.out`, outra usa `JOptionPane`. Não há código reutilizável entre elas. Uma classe abstrata sem código compartilhado seria funcionalmente idêntica a uma interface, mas bloquearia a possibilidade de as implementações herdarem de outra classe no futuro. A interface é a escolha mais limpa e permite extensibilidade máxima — adicionar uma terceira implementação (como um `LoggingUI` ou `WebUI`) requer apenas implementar a interface, sem restrições de herança.

### 3.20 Escolha da interface na inicialização

**Decisão:** usar `JOptionPane.showOptionDialog()` diretamente no método `main` de `FitManagerApp`, antes de qualquer instância de `UserInterface` existir.

**Alternativas consideradas:** usar `System.out`/`Scanner` para a pergunta inicial (rejeitado — é menos intuitivo para o usuário e inconsistente com o fato de que uma das opções é justamente a interface gráfica); criar uma `TerminalUI` provisória apenas para a pergunta (rejeitado — instanciar uma implementação concreta antes da escolha derrota o propósito da seleção).

**Justificativa:** `JOptionPane` é uma chamada estática que não depende de nenhuma instância — é o mecanismo mais direto para uma pergunta única fora da camada de interface. Esse uso pontual é explicitamente excepcional e documentado: ocorre apenas nesse momento, antes de qualquer menu existir. Após a escolha, a referência armazenada é do tipo `UserInterface` (nunca do tipo concreto), e é repassada ao `MainMenu` que a propaga para os submenus.

**Caso-limite:** se o usuário fecha o diálogo sem escolher (clica no X ou em Cancelar), `JOptionPane.showOptionDialog` retorna um valor que não corresponde a nenhuma opção válida. Nesse caso, o sistema encerra normalmente com `return` no `main` — sem mensagem de erro, sem loop, sem estado inconsistente. A decisão de encerrar (e não insistir) justifica-se pelo fato de que o sistema não pode funcionar sem uma interface de usuário, e insistir com diálogos repetidos seria inconveniente.

### 3.21 Consistência entre TerminalUI e JOptionPaneUI

**Decisão:** ambas as implementações oferecem as mesmas funcionalidades. A estratégia de consistência é garantida pelo contrato da interface — como ambas implementam os mesmos 5 métodos, os menus se comportam identicamente independentemente da implementação.

**Detalhes de implementação:**

- `TerminalUI.showMenu()` exibe título e opções com separadores visuais, lê via `Scanner.nextLine()`, e retorna a string digitada (Enter vazio retorna string vazia, tratada como inválida pelo menu).
- `JOptionPaneUI.showMenu()` exibe as opções em `JOptionPane.showInputDialog()`, retornando `null` quando o usuário cancela.
- `TerminalUI.getInput()` exibe o prompt e lê a entrada; Enter vazio retorna string vazia.
- `JOptionPaneUI.getInput()` usa `JOptionPane.showInputDialog()`, retornando `null` quando cancelado.
- `showScrollableMessage()` no `TerminalUI` exibe o texto com separadores visuais e pausa para leitura; no `JOptionPaneUI` utiliza um `JTextArea` dentro de `JScrollPane` com dimensão fixa (500×300), garantindo que listagens longas sejam navegáveis.

**Tratamento de `null`:** todos os menus verificam se o retorno de `showMenu()` e `getInput()` é `null` — quando é, a operação é cancelada e o controle retorna ao menu anterior. Essa convenção é uniforme em todo o sistema.

### 3.22 Propagação da UserInterface pelos menus

**Decisão:** manter a mesma abordagem da etapa anterior — o `MainMenu` recebe a instância de `UserInterface` no construtor e a propaga para os submenus via instanciação tardia (lazy).

**Mudança em relação à etapa 1:** nenhuma. A decisão da etapa anterior (seção 4.1) de usar instanciação tardia e receber `UserInterface` por parâmetro no construtor de cada menu se mostrou adequada para a refatoração. Como os menus já referenciavam `UserInterface` como tipo (nunca a implementação concreta), a conversão de classe para interface não exigiu nenhuma alteração nos menus.

### 3.23 Insuficiência da interface UserInterface

**Decisão:** estender a interface com um quinto método: `showScrollableMessage(String message)`.

**Justificativa:** durante a implementação da `JOptionPaneUI`, ficou claro que listagens longas em `JOptionPane.showMessageDialog` ficariam truncadas ou ilegíveis. A solução foi adicionar um método dedicado à exibição de conteúdo extenso. No `TerminalUI`, `showScrollableMessage` exibe o texto com separadores visuais e pausa; no `JOptionPaneUI`, usa `JTextArea` com scroll. A adição de um quinto método obrigou ambas as implementações a fornecê-lo — o que é o comportamento correto: se uma funcionalidade é necessária para o sistema, ambas as interfaces devem suportá-la.

### 3.24 Possibilidade de interface persistente como configuração

**Reflexão:** seria possível salvar a preferência de interface do usuário em um arquivo de configuração (ex.: `config.properties` ou `fitmanager.cfg`), que seria lido no `main` antes da exibição do diálogo de seleção. Se o arquivo existisse e contivesse uma preferência válida, o sistema pulularia diretamente com essa implementação; caso contrário, exibiria o diálogo normalmente. A arquitetura atual facilita essa evolução: a seleção ocorre em um único ponto (`FitManagerApp.main`), e a referência propagada já é do tipo `UserInterface` — bastaria adicionar a leitura do arquivo antes do `showOptionDialog`. Nenhuma outra classe precisaria ser alterada.

### 3.25 Subpacotes dentro de domain

**Decisão:** organizar as classes de domínio em subpacotes: `domain.model.plans` para a hierarquia de planos, `domain.model.payments` para a hierarquia de pagamentos, `domain.model.enums` para os enums, e `domain.model.filters` para os filtros polimórficos. `Student` e `Enrollment` permanecem diretamente em `domain.model`.

**Justificativa:** com a introdução de 4 subclasses de `Plan`, 4 subclasses de `Payment`, 5 filtros e 2 interfaces adicionais, o pacote `domain.model` teria 20+ arquivos em um único diretório — prejudicando a navegabilidade. A organização em subpacotes agrupa por hierarquia, facilita a localização e deixa a estrutura do projeto coerente com o diagrama de classes. O custo é um aumento moderado nas declarações de `import`, o que é aceitável pela melhoria na organização.

---

## 4. Como o polimorfismo simplificou o código

### 4.1 Cálculo do preço total — antes e depois

**Antes (Etapa 1):** `Plan` era uma classe concreta com um único `calculateTotalPrice(int months)` que aplicava a mesma regra para todos os tipos:

```java
// Plan.java — Etapa 1
public double calculateTotalPrice(int months) {
    double base = pricePerMonth * Math.min(months, minimumDuration);
    int extraMonths = Math.max(0, months - minimumDuration);
    double discounted = extraMonths * pricePerMonth * (1 - DISCOUNT_RATE);
    return base + discounted;
}
```

Não havia diferenciação por tipo — `MonthlyPlan` e `AnnualPlan` recebiam o mesmo desconto.

**Depois (Etapa 2):** cada subclasse implementa sua própria lógica. Exemplo do `AnnualPlan`:

```java
// AnnualPlan.java — Etapa 2
@Override
public double calculateTotalPrice(int months) {
    if (months <= 0) return 0;
    double grossTotal = getPricePerMonth() * months;
    if (months > getMinimumDuration()) {
        return grossTotal * (1 - DISCOUNT_RATE); // 15%
    }
    return grossTotal;
}
```

O `EnrollmentService` continua chamando `plan.calculateTotalPrice(months)` — uma única linha, sem nenhum `if` por tipo. O polimorfismo garante que a subclasse correta responde.

### 4.2 Taxa de cancelamento — código eliminado

**Antes (Etapa 1):** não havia taxa de cancelamento. Se tivesse sido implementada sem hierarquias, exigiria algo como:

```java
// Hipotético — sem polimorfismo
if (plan.getType() == PlanType.ANNUAL) {
    // calcular taxa de 20% se antes da metade...
} else {
    fee = 0;
}
```

**Depois (Etapa 2):** o `EnrollmentService` faz uma única chamada polimórfica:

```java
double cancellationFee = enrollment.getPlan().getCancellationFee(enrollment);
```

Cada subclasse retorna o valor correto: `AnnualPlan` calcula os 20% quando aplicável; `MonthlyPlan`, `QuarterlyPlan` e `SemiAnnualPlan` retornam 0.0. Nenhum `instanceof` necessário.

### 4.3 Resumo do pagamento — antes e depois

**Antes (Etapa 1):** `Payment` era uma classe concreta com apenas `toString()`. Todos os pagamentos exibiam as mesmas informações, independentemente do tipo.

**Depois (Etapa 2):** `getPaymentSummary()` é polimórfico — cada subclasse formata seu resumo com informações específicas. `CreditCardPayment` exibe parcelas, últimos dígitos do cartão e taxa de processamento; `CashPayment` exibe valor recebido e troco; `PixPayment` exibe a chave PIX. O menu chama `payment.getPaymentSummary()` sem saber o tipo concreto.

### 4.4 Taxa de processamento

**Antes (Etapa 1):** não existia o conceito de taxa de processamento.

**Depois (Etapa 2):** `payment.getProcessingFee()` retorna o valor correto para cada tipo sem condicionais. O `getEffectiveAmount()` na superclasse usa `amount - getProcessingFee()` de forma polimórfica — um único método concreto que funciona para todas as subclasses.

### 4.5 Filtros polimórficos no ReportsMenu

O método `showFilteredEnrollments(EnrollmentFilter filter)` no `ReportsMenu` é genérico — aplica qualquer filtro sem saber qual implementação concreta está sendo usada:

```java
private void showFilteredEnrollments(EnrollmentFilter filter) {
    OperationResult result = fitManager.listEnrollmentsByFilter(filter);
    // exibe resultados...
}
```

Para adicionar um novo tipo de relatório, basta criar uma nova classe que implemente `EnrollmentFilter` — sem alterar o `ReportsMenu`, o `FitManager` ou o `EnrollmentService`. Isso demonstra o princípio Open/Closed na prática.

---

## 5. Regras de negócio implementadas nesta etapa

| # | Regra | Classe e método |
|---|-------|----------------|
| 1 | Desconto de 5% no `QuarterlyPlan` quando `months > minimumDuration` | `QuarterlyPlan.calculateTotalPrice()` |
| 2 | Desconto de 10% no `SemiAnnualPlan` quando `months > minimumDuration` | `SemiAnnualPlan.calculateTotalPrice()` |
| 3 | Desconto de 15% no `AnnualPlan` quando `months > minimumDuration` | `AnnualPlan.calculateTotalPrice()` |
| 4 | Sem desconto no `MonthlyPlan` em nenhuma situação | `MonthlyPlan.calculateTotalPrice()` |
| 5 | Taxa de cancelamento de 20% do `totalPrice` no `AnnualPlan` antes da metade do período | `AnnualPlan.getCancellationFee()` |
| 6 | Isenção de taxa no `AnnualPlan` na metade ou após | `AnnualPlan.getCancellationFee()` |
| 7 | Sem taxa de cancelamento para `MonthlyPlan`, `QuarterlyPlan` e `SemiAnnualPlan` | Respectivos `getCancellationFee()` |
| 8 | Taxa de processamento de 2,5% no `CreditCardPayment` | `CreditCardPayment.getProcessingFee()` |
| 9 | Sem taxa de processamento para PIX, débito e dinheiro | Respectivos `getProcessingFee()` |
| 10 | `CashPayment`: valor recebido deve ser ≥ valor do pagamento | `EnrollmentService.validatePaymentParams()` |
| 11 | `CashPayment`: troco calculado como `amountReceived - amount` | `CashPayment.getChange()` |
| 12 | Pagamento registrado com data/hora exata (`LocalDateTime`) | `EnrollmentService.createPaymentByType()` usa `LocalDateTime.now()` |
| 13 | Seleção de interface na inicialização do sistema | `FitManagerApp.selectUserInterface()` |
| 14 | Ambas implementações de UI oferecem mesmas funcionalidades | `TerminalUI` e `JOptionPaneUI` implementam todos os 5 métodos de `UserInterface` |

---

## 6. Funcionalidades extras

### 6.1 Interface Summarizable

**O que foi implementado:** a interface `Summarizable` define o contrato `getSummary(): String`, implementada por `Student`, `Plan` (abstrata, com implementação concreta na superclasse), `Enrollment` e `Payment` (abstrata, com implementação concreta na superclasse). Cada classe fornece um resumo de uma linha com as informações mais relevantes.

**Classes criadas ou modificadas:** `Summarizable` (nova interface em `domain.model`); `Student`, `Plan`, `Enrollment`, `Payment` (adicionado `implements Summarizable` e método `getSummary()`).

**Conceitos aplicados:** interface Java como contrato polimórfico. O `ReportsMenu` chama `entity.getSummary()` para exibir qualquer entidade nas listagens compactas, sem depender de `toString()` ou de lógica de formatação espalhada nos menus.

**Decisão getSummary() vs. toString():** ambos coexistem com propósitos distintos. `toString()` retorna os dados completos da entidade (múltiplas linhas), adequado para consultas detalhadas e depuração. `getSummary()` retorna uma linha compacta, adequada para listagens onde dezenas de entidades são exibidas simultaneamente. No `ReportsMenu`, as listagens usam `getSummary()` e as consultas individuais usam `toString()` — essa separação torna as telas mais legíveis e demonstra que a mesma entidade pode ter múltiplas representações textuais com intenções diferentes.

**Quatro perguntas:**

1. *A funcionalidade agrega valor real ao domínio?* Sim — em uma academia real, a tela de listagem de alunos ou matrículas deve ser compacta e legível. Exibir o `toString()` completo de cada entidade em uma lista com 50 itens seria inviável.
2. *Aplica conceitos centrais desta etapa?* Sim — interface Java como contrato polimórfico, implementada por classes de domínio de hierarquias diferentes (`Student` não tem relação de herança com `Plan` nem com `Enrollment`).
3. *Está bem posicionada na arquitetura?* Sim — a interface reside em `domain.model`, as implementações nas classes de domínio, e o uso polimórfico no `ReportsMenu`.
4. *Há impacto em classes existentes?* Mínimo — adição de `implements Summarizable` e um método `getSummary()` em cada classe. Nenhuma remoção ou reestruturação.

### 6.2 Filtros polimórficos (EnrollmentFilter)

**O que foi implementado:** a interface `EnrollmentFilter` define dois métodos — `matches(Enrollment enrollment): boolean` e `getDescription(): String` — implementados por cinco classes concretas:

| Filtro | Critério |
|--------|----------|
| `ActiveEnrollmentFilter` | Status == ACTIVE |
| `CancelledEnrollmentFilter` | Status == CANCELLED |
| `PendingBalanceFilter` | `calculateBalance() > 0` |
| `ByPlanTypeFilter` | Plano do tipo especificado (filtro parametrizado) |
| `ExpiredEnrollmentFilter` | Status == ACTIVE e `isExpired() == true` |

O `EnrollmentService.listByFilter(EnrollmentFilter filter)` é um método genérico que aplica qualquer filtro à coleção. O `FitManager.listEnrollmentsByFilter(filter)` delega ao serviço. O `ReportsMenu.showFilteredEnrollments(filter)` exibe os resultados de forma polimórfica usando `getSummary()`.

**Classes criadas:** `EnrollmentFilter` (interface), `ActiveEnrollmentFilter`, `CancelledEnrollmentFilter`, `PendingBalanceFilter`, `ByPlanTypeFilter`, `ExpiredEnrollmentFilter` (5 implementações), todas em `domain.model.filters`.

**Classes modificadas:** `EnrollmentService` (adição de `listByFilter()`), `FitManager` (adição de `listEnrollmentsByFilter()`), `ReportsMenu` (reescrito com 13 opções, usando filtros polimórficos).

**Conceitos aplicados:** interface Java, polimorfismo (o mesmo método `showFilteredEnrollments` funciona com qualquer filtro), princípio Open/Closed (adicionar um novo filtro não requer alterar nenhuma classe existente).

**Quatro perguntas:**

1. *A funcionalidade agrega valor real ao domínio?* Sim — uma academia real precisa de relatórios filtrados: matrículas ativas, canceladas, inadimplentes, vencidas, por tipo de plano. São visões essenciais para a gestão.
2. *Aplica conceitos centrais desta etapa?* Sim — interface polimórfica com múltiplas implementações. O `ByPlanTypeFilter` é parametrizado (recebe `PlanType` no construtor), demonstrando que filtros podem ser genéricos e reutilizáveis.
3. *Está bem posicionada na arquitetura?* Sim — interfaces e implementações no domínio (`domain.model.filters`), lógica de filtragem no serviço (`EnrollmentService.listByFilter`), exibição no menu.
4. *Há impacto em classes existentes?* Adições apenas — `listByFilter()` no serviço e `listEnrollmentsByFilter()` no FitManager. Nenhuma remoção ou alteração de lógica existente.

### 6.3 Estatísticas gerais do sistema

O método `FitManager.getSystemStatistics()` consolida informações de múltiplas entidades: total de alunos, planos, matrículas, matrículas ativas e saldo pendente total. Acessível via opção "Estatísticas do sistema" no `ReportsMenu`.

### 6.4 Registro de pagamento com data/hora exata (LocalDateTime)

Os pagamentos são registrados com `LocalDateTime.now()` em vez de `LocalDate.now()`, capturando o momento exato do pagamento com precisão de nanossegundos. O `DateFormatter.formatDateTime()` exibe no formato `dd/MM/yyyy HH:mm:ss`. Essa informação é útil para auditoria e para distinguir pagamentos realizados no mesmo dia.

### 6.5 Dados de demonstração (DataMock)

A classe `mocks.DataMock` popula o sistema com cenários representativos controlados pela flag `DEV_MODE` em `FitManagerApp`.

### 6.6 Método genérico de busca com predicado

**O que foi implementado:** a classe utilitária `CollectionUtils`, no pacote `util`, define o método estático genérico `filter(ArrayList<T> source, Predicate<T> criterion)`. O método recebe uma coleção tipada e um critério de seleção, percorre a lista uma única vez e devolve um novo `ArrayList<T>` contendo apenas os elementos aprovados pelo predicado.

**Classes criadas:** `CollectionUtils`, em `util`.

**Classes modificadas:** `StudentService` e `EnrollmentService`. Os métodos de listagem que antes repetiam loops de filtragem passaram a delegar a iteração para `CollectionUtils.filter`, mantendo nos serviços apenas o critério de negócio.

**Conceitos aplicados:** generics (`<T>`), coleção genérica (`ArrayList<T>`) e interface funcional (`Predicate<T>`). Quando o critério já existe como método no contexto específico, ele é passado por referência de método, por exemplo `Student::isActive` e `filter::matches`.

**Decisão arquitetural:** o método foi colocado em `util` porque a estrutura da filtragem não pertence especificamente a alunos, planos ou matrículas. A regra de negócio continua nos serviços: `StudentService` decide que a listagem deve retornar alunos ativos; `EnrollmentService` decide critérios como matrícula ativa, saldo pendente ou histórico por CPF. O menu continua apenas exibindo resultados e não acessa coleções diretamente.

**Quatro perguntas:**

1. *A funcionalidade agrega valor real ao domínio?* Sim — sistemas de gestão de academia dependem de consultas frequentes por critérios, como alunos ativos, matrículas ativas, histórico de um aluno e matrículas com saldo pendente.
2. *Aplica conceitos centrais desta etapa?* Sim — o método é genuinamente genérico, pois funciona com qualquer tipo `T`; além disso, usa `Predicate<T>` para representar critérios de seleção sem duplicar a lógica de iteração.
3. *Está bem posicionada na arquitetura?* Sim — a operação transversal fica em `util`, enquanto os serviços preservam a responsabilidade de definir os critérios de negócio. Nenhuma regra foi movida para os menus.
4. *Há impacto em classes existentes?* Baixo — foram alterados apenas métodos de listagem em `StudentService` e `EnrollmentService`, sem mudar assinaturas públicas, mensagens de retorno ou regras de negócio.

---

## 7. Dificuldades e aprendizados da Etapa 2

### Dificuldades

- **Decidir o que pertence à superclasse vs. subclasse.** A tentação inicial era colocar tudo na superclasse para "reaproveitar". A análise cuidadosa revelou que atributos como `pixKey`, `installments` e `amountReceived` não fazem sentido para todas as subclasses — e colocá-los na superclasse criaria uma abstração incorreta. A regra "se o atributo é nulo ou sem sentido em pelo menos uma subclasse, ele não pertence à superclasse" foi o guia.

- **Manter a retrocompatibilidade com a etapa 1.** A maior dificuldade foi garantir que os 8 fluxos críticos da etapa anterior continuassem funcionando após cada transformação. A estratégia adotada foi refatorar uma hierarquia por vez (primeiro `Plan`, depois `Payment`, depois `UserInterface`) e testar todos os fluxos após cada etapa. Isso evitou que erros se acumulassem e ficassem difíceis de rastrear.

- **Migração de `LocalDate` para `LocalDateTime` em Payment.** A decisão de registrar pagamentos com data/hora exata exigiu atualização em todas as subclasses de `Payment`, no `EnrollmentService` e no `DateFormatter`. A propagação foi manual e exigiu atenção para não esquecer nenhum ponto — um `import java.time.LocalDate` remanescente em `Payment.java` foi identificado e precisa ser removido.

- **Bug no `createPaymentByType` para CASH.** Durante a implementação, uma variável foi incorretamente atribuída (`amount` ao invés de `amountReceived`), o que fazia com que o troco fosse calculado incorretamente. Esse tipo de erro sutil — onde o código compila mas produz resultado errado — reforça a importância de testes manuais com cenários de borda.

- **Conversão de `UserInterface` de classe para interface.** A conversão em si foi trivial porque os menus já referenciavam `UserInterface` como tipo. O desafio real foi implementar a `JOptionPaneUI` com comportamento equivalente à `TerminalUI` — especialmente para listagens longas (resolvido com `showScrollableMessage` e `JScrollPane`).

### Aprendizados

- **O polimorfismo simplifica de verdade.** A eliminação de condicionais por tipo não é um benefício teórico — ao remover o `switch` genérico do `calculateTotalPrice` da etapa 1 e substituí-lo por quatro implementações especializadas, o código ficou mais curto, mais legível e mais fácil de estender. Adicionar um quinto tipo de plano agora é uma operação localizada.

- **Hierarquias devem emergir do domínio.** Criar `MonthlyPlan`, `QuarterlyPlan`, `SemiAnnualPlan` e `AnnualPlan` não foi uma decisão arbitrária — cada subclasse tem regras genuinamente diferentes. Uma hierarquia que existe apenas para "usar herança" sem comportamentos distintos seria artificial.

- **Interfaces como contratos multiplicam o valor.** A interface `Summarizable` é simples (um único método), mas transforma a forma como listagens funcionam: qualquer entidade que a implemente pode ser exibida de forma compacta em qualquer relatório, sem código específico. O mesmo vale para `EnrollmentFilter` — cinco implementações, um único método de filtragem no serviço.

- **Decisões da etapa anterior facilitaram a refatoração.** O fato de os menus já referenciarem `UserInterface` como tipo (nunca a implementação concreta), de o `FitManager` ser o único ponto de coordenação, e de o `OperationResult` ser o padrão uniforme de retorno fez com que a introdução das hierarquias e da interface exigisse modificações mínimas nas camadas superiores. Essa é a recompensa prática de boas decisões arquiteturais.

### O que faríamos diferente

- Estabelecer desde o início da etapa 2 quais métodos seriam abstratos em cada superclasse, antes de escrever qualquer código — a análise no papel economiza tempo de refatoração no editor.
- Criar cenários de teste mais abrangentes no `DataMock` para cobrir os novos fluxos (cancelamento com taxa, pagamento em dinheiro com troco, matrícula com desconto) desde o início do desenvolvimento.
- Remover imports não utilizados como parte do checklist de cada commit, evitando que fiquem acumulados.


---

# Relatório — FitManager (Etapa 3)

## 1. Introdução da Etapa 3

A Etapa 3 consolida o FitManager em quatro frentes de trabalho, todas integradas à
arquitetura em camadas já estabelecida nas etapas anteriores: **(1) generics**, com a
parametrização de `OperationResult<T>`, a introdução da classe genérica abstrata
`Repository<T>` e do método genérico `CollectionUtils.filter`; **(2) tratamento de
exceções**, com uma hierarquia personalizada organizada em categorias (validação,
regra de negócio e persistência) a partir de exceções-base do sistema; **(3)
persistência em arquivos**, gravando e recuperando todo o estado entre sessões em
arquivos de texto (CSV) com preservação dos tipos polimórficos; e **(4) o relatório
financeiro mensal**, única funcionalidade inteiramente nova, que agrega receita por
período aproveitando o polimorfismo das hierarquias da Etapa 2.

A etapa combinou **refatoração** de código existente (parametrização de tipos,
extração da camada de repositório, centralização da validação de entradas) com
**adição** de funcionalidades novas (persistência, relatório). A diretriz que guiou
todas as decisões foi a coerência arquitetural: cada mecanismo novo foi posicionado
na camada adequada, e nenhuma funcionalidade das Etapas 1/2 foi regredida no processo.

## 2. Diagrama de classes atualizado

O diagrama foi atualizado em `diagram.puml` (renderizado em `diagram.png`/`diagram.jpg`)
e agora reflete o sistema ao final da Etapa 3: `OperationResult<T>` com o parâmetro de
tipo, a classe genérica `Repository<T>` com `save()`/`load()` abstratos e seus três
repositórios concretos, a hierarquia completa de exceções (duas raízes — `FitManagerException`
e `PersistenceException`), os pacotes novos `persistence` e `exceptions`, as factories
`PlanFactory`/`PaymentFactory`, a classe de resultado `FinancialReport` e o utilitário
genérico `CollectionUtils`. A versão da Etapa 2 foi preservada como `diagram-parte-2.png`.

## 3. Decisões de projeto da Etapa 3

Cada decisão abaixo segue o formato **decisão → alternativas consideradas → critério →
impacto**, com referência a classes e métodos reais do sistema.

### 3.1 Convenção para operações sem dado de retorno

**Decisão:** operações que comunicam apenas sucesso/falha, sem objeto associado
(`updatePlanPrice`, `removeStudent`), usam `OperationResult<Void>` com `data = null`.
**Alternativas:** `OperationResult<Object>` (perde a intenção e reabre espaço para
casts) ou criar uma classe separada `VoidResult` (duplicaria o tipo). **Critério:**
`java.lang.Void` existe exatamente para representar ausência de valor em contexto
genérico, e manter o mesmo tipo (`OperationResult<T>`) em todo o sistema preserva a
uniformidade — o menu trata qualquer retorno da mesma forma (`isSuccess()` +
`getMessage()`). **Impacto:** convenção única e previsível; qualquer operação sem dado
é imediatamente reconhecível pela assinatura `OperationResult<Void>`.

### 3.2 Estratégia de refatoração em cascata de `OperationResult<T>`

**Decisão:** a parametrização foi feita em um branch dedicado
(`feature/generic-operation-result`), avançando por camadas — primeiro a classe
`OperationResult`, depois os serviços, o `FitManager` e por fim os menus — conforme o
histórico de commits ("Atualiza os Services...", "Atualiza Menus...", "Atualiza os dados
de exemplo..."). **Alternativas:** alterar tudo de uma vez (deixaria o projeto sem
compilar por um longo período) ou usar um tipo intermediário temporário. **Critério:**
parametrizar primeiro a classe e seguir camada a camada mantém o projeto compilando em
cada passo e isola o que quebra. **Impacto:** como `OperationResult` afeta todos os
serviços e menus, integrá-lo cedo fez com que as demais branches da etapa já
trabalhassem sobre a base parametrizada, reduzindo conflitos de merge.

### 3.3 Repositório genérico — o que é genuinamente comum entre os serviços

**Decisão:** `Repository<T>` (abstrata, em `persistence`) centraliza o que era
estruturalmente idêntico nos três serviços: a coleção tipada `ArrayList<T> items` e as
operações `listAll()`, `count()`, `isEmpty()`, `add(T)` e `remove(T)`; além disso,
declara `save()` e `load()` **abstratos**. **Alternativas:** generalizar também busca e
validação (rejeitado) ou centralizar só `listAll()` (eliminaria pouca duplicação).
**Critério:** os critérios de busca diferem por domínio (aluno por CPF em
`StudentRepository.findByCpf`, plano por nome, matrícula por código) e as validações são
específicas — portanto permanecem nos serviços/repositórios concretos; só o que tem a
**mesma assinatura e a mesma lógica** subiu para a abstração. **Impacto:** a duplicação
real da coleção e das operações estruturais foi eliminada, e `save()`/`load()` abstratos
criam um contrato verificado em tempo de compilação que qualquer repositório futuro é
obrigado a cumprir.

### 3.4 Herança ou composição para o `Repository<T>`

**Decisão:** os repositórios **concretos** herdam de `Repository<T>`
(`StudentRepository extends Repository<Student>`), mas os **serviços** se relacionam com
os repositórios por **composição** (`StudentService` tem um atributo
`private StudentRepository repository`). **Alternativas:** `StudentService extends
Repository<Student>` (herança direta serviço↔repositório). **Critério:** o teste
semântico da Etapa 2 — um `StudentService` *não é* um repositório; ele *usa* um
repositório para armazenar e persistir alunos. Herdar misturaria duas responsabilidades
(regra de negócio + estrutura de armazenamento) na mesma classe. **Impacto:** o serviço
mantém responsabilidade única (regras de negócio) e delega armazenamento/persistência ao
repositório composto, exposto via `getRepository()` para o `FitManager` coordenar a carga
e a gravação.

### 3.5 Parâmetro de tipo limitado (`T extends ...`) é necessário?

**Decisão:** **não** usar parâmetro de tipo limitado em `Repository<T>` — `T` é
irrestrito. **Alternativas:** criar uma interface `Identifiable` e usar
`Repository<T extends Identifiable>` para padronizar buscas por identificador.
**Critério:** as operações que sobem para a abstração (`listAll`, `count`, `isEmpty`,
`add`, `remove`) não chamam nenhum método específico de `T` — elas operam sobre a
coleção, não sobre o conteúdo. A identificação (CPF, nome, código) é heterogênea e fica
nos concretos. Forçar um parâmetro limitado adicionaria uma interface e complexidade sem
necessidade real. **Impacto:** abstração mais simples; se no futuro a busca por
identificador precisar subir para a superclasse, a introdução de `Identifiable` será o
próximo passo natural.

### 3.6 `nextCode` como caso especial do `EnrollmentService`

**Decisão:** o contador estático `Enrollment.nextCode` é persistido como uma **diretiva
de comentário** no topo de `enrollments.txt` (`# nextCode=N`) e restaurado na leitura via
`Enrollment.setNextCode(int)`. A gravação/leitura dessa diretiva fica dentro de
`EnrollmentRepository.save()/load()`, sem que `Repository<T>` conheça o detalhe.
**Alternativas:** arquivo separado só para o contador, ou recomputar `nextCode` como
`max(código)+1` na carga (frágil se houver remoções). **Critério:** o `nextCode` pertence
à lógica do `EnrollmentService`, não à estrutura genérica; gravá-lo como diretiva no
próprio arquivo de matrículas mantém tudo num lugar só e a abstração genérica intacta.
**Impacto:** após reiniciar, a próxima matrícula recebe o código seguinte ao último
usado, sem repetição — verificável reabrindo o sistema e criando uma nova matrícula.

### 3.7 `ArrayList` vs `List` como tipo de referência

**Decisão:** manter `ArrayList<T>` como tipo concreto nas assinaturas (ex.:
`OperationResult<ArrayList<Student>>`, `Repository.listAll(): ArrayList<T>`), preservando
a convenção das Etapas 1/2. **Alternativas:** migrar para `List<T>` (programar para a
interface). **Critério:** a mudança não traria benefício concreto no estado atual (não há
intenção de trocar a implementação por `LinkedList`), e revisar todas as assinaturas
introduziria risco de regressão sem ganho — contrariando o princípio de não regredir.
**Impacto:** consistência com o código existente; a migração para `List` continua sendo
uma melhoria de baixo custo caso uma implementação alternativa passe a ser necessária.

### 3.8 Exceções verificadas vs não verificadas (política por categoria)

**Decisão:** duas raízes intencionalmente separadas. As exceções de **domínio**
(`FitManagerException extends RuntimeException` → `ValidationException`,
`BusinessException` e suas folhas) são **não verificadas**; as de **persistência**
(`PersistenceException extends Exception` → `CorruptedFileException`,
`WriteFailureException`) são **verificadas**. **Critério (Effective Java, item 70):** use
verificadas para condições das quais o chamador pode razoavelmente se recuperar e que
precisa conhecer em tempo de compilação. Uma falha de arquivo na inicialização exige que
o chamador (`FitManagerApp`) decida conscientemente o que fazer (iniciar vazio, avisar,
encerrar) — daí ser verificada. Já uma falha de validação/negócio é tratada de forma
centralizada nos menus com `catch (FitManagerException)`, e espalhar `throws` por toda a
cadeia seria mais ruído do que informação — daí ser não verificada. **Três exemplos:**
`RequiredFieldException` (validação, não verificada, lançada em
`StudentService.validateRequiredFields`); `StudentWithActiveEnrollmentException` (negócio,
não verificada, lançada em `FitManager.removeStudent`); `CorruptedFileException`
(persistência, verificada, lançada em `StudentRepository.load`). **Impacto deliberado:**
`catch (FitManagerException e)` **não** captura falhas de persistência — a fronteira entre
camadas é preservada pelo próprio sistema de tipos.

### 3.9 Quando lançar exceção e quando retornar `OperationResult` com falha

**Decisão:** `OperationResult(success = false)` para resultados **normais e esperados** do
fluxo (CPF não encontrado, lista vazia, CPF duplicado, data futura, período sem dados);
**exceção** para o que **quebra o fluxo** (campo obrigatório ausente, formato inválido na
conversão, falha de arquivo). **Critério:** `OperationResult` comunica o desfecho de algo
que o usuário pediu e o sistema avaliou; a exceção escala uma situação em que o código não
consegue continuar normalmente. **Exemplos concretos:** CPF duplicado em
`StudentService.registerStudent` → `OperationResult` com falha; campo obrigatório vazio →
`RequiredFieldException`; arquivo ilegível em `EnrollmentRepository.load` →
`CorruptedFileException`. **Impacto:** o chamador sabe o que esperar de cada método sem ler
a implementação — fluxo previsível e uniforme.

### 3.10 Onde lançar e onde capturar (mapa por camada)

**Decisão e mapa:** exceções de **validação/negócio** são lançadas nos serviços/`FitManager`
e capturadas nos **menus** (`StudentMenu`, `PlanMenu`, `EnrollmentMenu`, `MainMenu`,
`ReportsMenu`) por `catch (FitManagerException e) { ui.showError(e.getMessage()); }`.
Exceções de **persistência** são lançadas nos repositórios e capturadas **fora dos menus**,
no `FitManagerApp` (`loadData`/`saveData`) — nunca num menu. **Critério:** a camada que
detecta o problema lança; a que sabe comunicá-lo ao usuário captura. Um menu capturando
`PersistenceException` significaria que a apresentação conhece detalhes de infraestrutura.
**Impacto:** as fronteiras de camada são respeitadas e verificáveis — uma busca por
`PersistenceException` em `src/ui` retorna vazio.

### 3.11 Relançar, encapsular ou tratar localmente

**Decisão:** os repositórios **encapsulam** as exceções de I/O da API Java em exceções da
hierarquia de persistência. Em `StudentRepository.load`, um `IOException` vira
`CorruptedFileException`; em `save`, um `IOException` vira `WriteFailureException` (ambos
preservando a causa original via `new ...Exception(path, e)`). **Alternativa:** deixar
`IOException` vazar até o `FitManager`. **Critério:** o `FitManager`/`FitManagerApp` não
precisa saber que a persistência usa `BufferedWriter`/arquivos — só que houve falha de
persistência. **Impacto:** o mecanismo de persistência poderia ser trocado (ex.: binário)
sem afetar as camadas superiores; o encapsulamento mantém o acoplamento baixo.

### 3.12 Validação de entradas: menu ou `UserInterface`?

**Decisão:** centralizar a conversão e o tratamento na `UserInterface`, com a lógica
compartilhada na abstrata `BaseUserInterface`. Os métodos `getInt`, `getDouble`, `getDate`
e `showMenu` convertem a entrada, exibem o formato esperado em caso de erro e repetem a
solicitação; o parsing genérico é feito por `readAndParse(...)` usando a interface
funcional `UserInputParser<T>`. **Alternativa:** `try-catch` repetido em cada menu.
**Critério:** centralizar elimina a duplicação e garante comportamento idêntico em todos os
menus; o custo (colocar conversão na UI) é aceitável porque `BaseUserInterface` isola essa
responsabilidade das primitivas de I/O das UIs concretas. **Impacto:** nenhum
`NumberFormatException`/`DateTimeParseException` chega ao terminal; `TerminalUI` e
`JOptionPaneUI` herdam o mesmo comportamento de validação.

### 3.13 Texto vs binário; como o tipo concreto é gravado e lido

**Decisão:** **arquivos de texto (CSV, delimitador `;`)** em `data/`. O **tipo concreto**
é gravado como campo da linha — primeiro campo em `plans.txt` (`type`) e segundo em
`payments.txt` — e reconstruído na leitura via `PlanFactory.create(...)` e
`PaymentFactory.create(...)`. **Alternativa:** serialização binária (`ObjectOutputStream`),
que preservaria o tipo automaticamente. **Critério:** texto pode ser inspecionado e
corrigido em qualquer editor, o que facilitou enormemente o desenvolvimento e a depuração;
o custo (escrever a conversão objeto↔texto) é compensado por essa transparência, e a
serialização binária traria fragilidade de versionamento. **Impacto:** após a recarga,
`MonthlyPlan`/`AnnualPlan` etc. respondem corretamente a `calculateTotalPrice()` e
`getCancellationFee()`, e cada `Payment` preserva seus atributos específicos — o
polimorfismo sobrevive ao ciclo. A escrita dos campos específicos de pagamento foi feita de
forma polimórfica via `Payment.getCsvExtraFields()` (sem `instanceof`), espelhando a leitura.

### 3.14 Referências cruzadas: identificadores vs objeto inteiro

**Decisão:** a matrícula grava apenas **identificadores** das suas referências — CPF do
aluno e nome do plano — e as reconstitui na leitura buscando os objetos já carregados em
memória (`resolvePlan` e a verificação por `studentRepository.findByCpf`). **Alternativa:**
gravar o objeto inteiro de aluno e plano dentro da matrícula (duplicação e risco de
inconsistência se o aluno fosse atualizado depois). **Critério:** cada entidade existe em um
único lugar no arquivo; a referência é reconstruída por identificador. **Impacto:** impõe
uma ordem de carga (alunos e planos antes das matrículas) e um tratamento para o caso fora
de sincronia: se uma matrícula referencia um CPF/plano inexistente, a leitura lança
`CorruptedFileException` identificando a linha, em vez de criar uma referência nula
silenciosa.

### 3.15 Quando sincronizar memória e arquivo

**Decisão:** persistir **no encerramento** (opção "Sair" do menu principal), via
`FitManager.saveAll()`. **Alternativa:** salvar a cada operação relevante. **Critério:**
salvar no encerramento é eficiente e simples; salvar a cada operação introduziria I/O
constante. **Impacto (reconhecido explicitamente):** um encerramento inesperado (queda de
energia, kill do processo) perde as alterações da sessão. Essa é uma limitação conhecida e
aceita; a funcionalidade extra de gravação atômica/backup foi considerada (ver §3.18) como
evolução futura.

### 3.16 Onde fica a responsabilidade de persistência

**Decisão:** a persistência reside em um **pacote `persistence` dedicado**, com um
repositório por entidade; o `FitManager` apenas **coordena** a ordem (`loadAll`/`saveAll`) e
os serviços apenas conhecem seu repositório por composição. **Alternativas:** colocar
`save/load` direto nos serviços (acumularia duas responsabilidades) ou um único
`DataManager` central (menos coeso, conheceria todas as entidades ao mesmo tempo).
**Critério:** separar a persistência preserva a responsabilidade única e a arquitetura em
camadas; o custo (mais classes) é compensado pela coesão. **Impacto:** o domínio não importa
`persistence`; a camada é claramente identificável no projeto.

### 3.17 O que caracteriza "arquivo corrompido" e o que o sistema faz

**Decisão:** considera-se corrompido um arquivo com linha de campos insuficientes, campo que
não converte para o tipo esperado (data, número, enum) ou referência cruzada inexistente.
Cada caso lança `CorruptedFileException` com a identificação do arquivo e da linha (ex.:
`"linha 4 com número insuficiente de campos"`, `"data inválida na linha 3"`, `"aluno com CPF
... referenciado na linha 5 não encontrado"`). **Política ao detectar:** a leitura é
interrompida e a falha sobe como `PersistenceException`; o `FitManagerApp` informa o usuário
e segue com o que foi carregado até o erro, sem encerrar abruptamente. **Critério:**
interromper e comunicar é mais seguro do que ignorar silenciosamente registros (que
esconderia perda de dados). **Impacto:** o usuário sempre sabe qual arquivo e qual linha
causaram o problema.

### 3.18 Falha parcial de gravação no encerramento

**Decisão:** `saveAll()` grava na ordem `enrollments → plans → students`; se um repositório
falhar, a exceção sobe e o `FitManagerApp` (`saveData`) **avisa o usuário** que as
alterações da sessão podem não ter sido salvas — o sistema nunca encerra silenciosamente.
**Alternativa avaliada (não implementada):** gravação atômica em dois passos (arquivo
temporário + rename) e backup automático antes da sobrescrita. **Critério:** a comunicação
clara da falha é o requisito mínimo inegociável; a atomicidade plena ficou registrada como
melhoria futura por custo/benefício. **Impacto:** não há perda silenciosa; reconhece-se que
uma falha no meio da sequência pode deixar arquivos de sessões diferentes (limitação
conhecida).

### 3.19 O paradoxo da interface na inicialização e o `DEV_MODE`

**Decisão:** a interface é escolhida **primeiro** (`selectUserInterface()` via `JOptionPane`,
antes de qualquer UI existir), e só **depois** os dados são carregados (`loadData`), de modo
que qualquer erro de carga já possa ser comunicado pela UI escolhida. O `FitManagerApp` foi
reorganizado em métodos de responsabilidade única (`selectUserInterface`, `loadData`,
`prepareInitialData`, `saveData`). **Sobre o `DEV_MODE`:** a constante `DEV_MODE` (default
`false`) popula dados de demonstração (`DataMock`) apenas quando o sistema inicia vazio;
mantida `false` para a avaliação, a primeira execução inicia vazia, exibindo apenas uma dica
informativa de que essa opção existe. **Critério:** resolve a dependência circular
"carregar precisa de UI / escolher UI vem antes" carregando após a escolha. **Impacto:**
inicialização limpa e previsível; o erro de carga sempre tem uma UI para ser exibido.

### 3.20 Onde reside a lógica de agregação do relatório

**Decisão:** o cálculo é coordenado por `FitManager.generateMonthlyReport(month, year)`, que
itera as matrículas e alimenta um objeto de **resultado** `FinancialReport` (em
`application.reports`); o `ReportsMenu` apenas solicita mês/ano e exibe/exporta.
**Alternativas:** lógica no menu (rejeitado — regra de negócio na apresentação) ou um
`ReportService` separado. **Critério:** `FinancialReport` é classe de resultado (guarda
métricas e sabe formatar/exportar), e o `FitManager` já tem acesso às coleções dos serviços,
sendo o coordenador natural. **Impacto:** o menu não calcula nada; a lógica fica testável e
fora da camada de UI.

### 3.21 Como agrupar por tipo sem `instanceof`

**Decisão:** os agrupamentos usam `Map<String, Double>`/`Map<String, Integer>` com a chave
vinda de `Plan.getTypeName()` e `Payment.getTypeName()` — método polimórfico que cada
subclasse já responde. **Alternativa:** `instanceof`/`getClass()` para identificar o tipo
concreto. **Critério:** condicionais por tipo são exatamente o que o polimorfismo da Etapa 2
elimina; `getTypeName()` deixa cada objeto se identificar. **Impacto:** o relatório inteiro
opera sobre os tipos abstratos `Plan` e `Payment`; **não há nenhum `instanceof` no projeto**
(inclusive a escrita de pagamentos foi convertida para `getCsvExtraFields()`).

### 3.22 Período sem dados é resultado válido (e o princípio de não retornar `null`)

**Decisão:** um período sem pagamentos retorna `OperationResult<FinancialReport>` com
**sucesso** e um `FinancialReport` com métricas **zeradas** e mensagem informativa
(`isEmpty()` controla a nota exibida em `format()`) — nunca exceção, erro ou `null`.
**Critério:** ausência de dados é um desfecho legítimo da consulta; retornar `null`
transferiria a todos os chamadores o peso de verificar `null` antes de usar. **Impacto:** o
menu trata o relatório vazio como qualquer outro. O mesmo princípio orienta o resto do
sistema (métodos preferem `OperationResult`/objeto vazio a `null`).

### 3.23 Regra de negócio específica: data de inauguração (01/03/2026)

**Decisão:** a FitManager foi inaugurada em 01/03/2026; o `ReportsMenu`
(`INAUGURATION_YEAR = 2026`, `INAUGURATION_MONTH = 3`) só gera relatórios a partir desse
período — rejeita ano < 2026 e o par ano = 2026 com mês < 3; de 2027 em diante todos os
meses (incluindo janeiro e fevereiro) ficam disponíveis. O mês permanece restrito a 1–12.
**Alternativa:** aceitar qualquer mês 1–12 de qualquer ano e sempre exibir zerado.
**Critério:** refletir a existência real da academia — não há receita antes da fundação.
**Impacto:** períodos válidos sem movimento ainda exibem o relatório zerado (conforme o
enunciado); apenas períodos anteriores à inauguração são bloqueados, com mensagem clara.

## 4. Como os generics eliminaram duplicação e melhoraram a segurança de tipos

**4.1 `OperationResult<T>` — fim dos casts.**
Antes (Etapa 1), o dado era `Object` e cada menu fazia cast:

```java
// Antes — OperationResult com Object
private Object data;
public Object getData() { return data; }

// No menu:
Student aluno = (Student) result.getData(); // cast não verificado → risco de ClassCastException
```

Depois (Etapa 3):

```java
// Depois — OperationResult<T>
private T data;
public T getData() { return data; }

// No menu:
Student aluno = result.getData(); // sem cast — o compilador garante o tipo
```

Não há mais nenhum cast sobre `getData()` no projeto, e o compilador verifica a consistência
em tempo de compilação.

**4.2 `Repository<T>` — fim da coleção e das operações duplicadas.**
Antes, cada serviço mantinha sua própria coleção e repetia as operações estruturais:

```java
// Antes — duplicado em StudentService, PlanService, EnrollmentService
private ArrayList<Student> students = new ArrayList<>();
public OperationResult<ArrayList<Student>> listAll() { ... itera students ... }
```

Depois, a coleção tipada e as operações comuns vivem uma única vez em `Repository<T>`, e os
serviços compõem o repositório concreto:

```java
// Depois — Repository<T> (uma vez)
protected ArrayList<T> items = new ArrayList<>();
public ArrayList<T> listAll() { return new ArrayList<>(items); }
public int count() { return items.size(); }
// StudentRepository extends Repository<Student>; StudentService tem um StudentRepository
```

**4.3 `CollectionUtils.filter` — fim do laço de filtragem repetido.**
A iteração "percorrer e selecionar por critério" se repetia. Foi extraída para um método
genérico com `Predicate<T>`:

```java
public static <T> ArrayList<T> filter(ArrayList<T> source, Predicate<T> criterion) {
    ArrayList<T> result = new ArrayList<>();
    for (T item : source) if (criterion.test(item)) result.add(item);
    return result;
}
```

Usado com lambdas/method references em `StudentService.listAll()` (`Student::isActive`) e em
`EnrollmentService` (histórico por aluno e `listByFilter` via `filter::matches`).

**4.4 Coleções tipadas em todo o sistema.** Não há coleção bruta; os agrupamentos do
relatório usam `Map<String, Double>`. O projeto compila com `-Xlint:unchecked` sem warnings
de tipo não verificado.

## 5. Política de exceções e estratégia de persistência (resumo)

**Exceções.** Hierarquia em `exceptions/` com duas raízes: `FitManagerException`
(RuntimeException, não verificada) → `ValidationException` (`RequiredFieldException`,
`InvalidFormatFieldException`) e `BusinessException` (`StudentWithActiveEnrollmentException`,
`DuplicatedEnrollmentException`, `DuplicatedPlanException`); e `PersistenceException`
(Exception, verificada) → `CorruptedFileException`, `WriteFailureException`. Lançamento nos
serviços/repositórios; captura de domínio nos menus (`catch FitManagerException`) e de
persistência no `FitManagerApp`. Nenhum `catch` vazio; nenhuma stack trace chega ao terminal.

**Persistência.** Texto/CSV em `data/` (`students.txt`, `plans.txt`, `enrollments.txt`,
`payments.txt`); tipo concreto no campo de tipo + factories na leitura; `nextCode` como
diretiva `# nextCode=N`; ordem de carga alunos/planos → matrículas e gravação inversa
coordenadas por `FitManager.loadAll/saveAll`; `try-with-resources` em toda leitura/escrita;
arquivo ausente = início vazio silencioso; arquivo corrompido = `CorruptedFileException` com
arquivo/linha; falha de escrita = aviso ao usuário sem encerrar em silêncio.

## 6. Funcionalidades extras

### 6.1 Busca genérica por predicado (`CollectionUtils.filter`)

Método genérico `static <T> ArrayList<T> filter(ArrayList<T>, Predicate<T>)` em
`util.CollectionUtils`, reaproveitado em três pontos com lambdas/method references.

1. **Agrega valor ao domínio?** Sim — centraliza a operação "filtrar uma coleção por um
   critério", que aparecia repetida (alunos ativos, matrículas de um aluno, matrículas por
   status/tipo). Uma academia real lista subconjuntos o tempo todo.
2. **Aplica um conceito central da etapa de forma genuína?** Sim — é um método **genérico**
   sobre `ArrayList<T>` com `java.util.function.Predicate<T>` (interface funcional),
   acionado por expressões lambda e method references. Não poderia ser feito assim sem
   generics.
3. **Está bem posicionada na arquitetura?** Sim — o utilitário fica em `util` (sem regra de
   negócio); a lógica de **qual** critério aplicar permanece nos serviços. Exemplo de lambda
   anotado: em `EnrollmentService`, `CollectionUtils.filter(repository.listAll(), e ->
   e.getStudentCpf().equals(cpf))` — o `Predicate<Enrollment>` seleciona as matrículas cujo
   CPF do aluno é igual ao informado.
4. **Quais classes existentes foram modificadas?** `StudentService.listAll()` e dois métodos
   de `EnrollmentService` (`listHistoryByStudent` e `listByFilter`) passaram a delegar o laço
   ao utilitário; `listByFilter` usa `filter::matches`, integrando o padrão Strategy
   (`EnrollmentFilter`) ao predicado genérico — sem risco para o restante do sistema.

### 6.2 Outras extensões já presentes

Exportação do relatório financeiro para CSV em `data/reports/` (com tratamento de
`IOException` sem encerrar) e o modo de demonstração `DataMock` (controlado por `DEV_MODE`)
complementam a etapa, exercitando persistência e tratamento de exceções.

## 7. Dificuldades e aprendizados da Etapa 3

A maior dificuldade foi conduzir **refatoração e adição simultâneas** sem regredir as Etapas
1/2: a parametrização de `OperationResult<T>` tocou todos os menus e serviços de uma vez, e
foi preciso avançar por branch dedicada e camada a camada para manter o projeto compilando. A
persistência polimórfica revelou o ponto mais técnico — a escrita inicial dos campos
específicos de pagamento usava `instanceof`, o que foi posteriormente substituído pelo método
polimórfico `Payment.getCsvExtraFields()`, deixando leitura e escrita simétricas e o sistema
livre de condicionais por tipo. A validação de entradas, antes dispersa, mostrou o valor de
centralizar comportamento em `BaseUserInterface`.

Decisões anteriores que **facilitaram** esta etapa: os menus já referenciavam
`UserInterface` (e não a implementação concreta), o `FitManager` já era o único ponto de
coordenação, e o `OperationResult` já era o padrão de retorno — o que limitou o alcance das
refatorações. O que **precisou ser revisto**: o campo `data: Object` (dívida técnica
assumida na Etapa 1) foi finalmente parametrizado; e a validação de mês do relatório, que
inicialmente bloqueava meses fora de 3–12 de forma rígida, foi corrigida para refletir
corretamente a regra de inauguração (permitindo jan/fev a partir de 2027). **O que faríamos
diferente:** definir a fronteira exceção × `OperationResult` no papel antes de codar, e
planejar o formato dos arquivos de persistência (campos, ordem, diretiva de `nextCode`) antes
de implementar a leitura/escrita.

## 8. Contribuições individuais (Etapa 3)

As contribuições seguem o histórico de commits do repositório, organizadas pelas branches de
funcionalidade desta etapa (`feature/generic-operation-result`, `feature/generic-repository`,
`feature/file-persistence`, `feature/robust-input-validation`, `feature/financial-report`,
`feature/metodo-busca-predicado`), integradas a `stage-3` via pull requests revisados.

- **Gabriel Felipe Barbosa** — parametrização de `OperationResult<T>` e propagação em
  serviços/menus; estrutura genérica `Repository<T>` e repositórios concretos; persistência
  em arquivo (gravação/leitura, factories, `nextCode`); relatório financeiro; remoção do
  `instanceof` na persistência; reorganização do `FitManagerApp`; documentação (README,
  diagrama, relatório).
- **Marcelle Luna Souza** — hierarquia de exceções e sua aplicação nos serviços/menus;
  validação robusta de entradas centralizada na `UserInterface`/`BaseUserInterface`; revisões
  de pull request sobre corretude das exceções e consistência dos tipos genéricos; apoio na
  persistência e nos dados de demonstração.

> Observação: a distribuição acima deve ser conferida e ajustada pelo grupo conforme o
> histórico real de cada integrante no repositório antes da entrega, garantindo coerência
> com os commits.
