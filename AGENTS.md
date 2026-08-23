# AGENTS.md — Delta API PostgreSQL

Este arquivo orienta agentes de IA e pessoas que trabalham no `delta-api-postgres`. Leia-o por completo antes
de alterar qualquer arquivo e confirme as instruções específicas da tarefa no `TASK.md` local.

---

## 1. Visão geral do Projeto Delta

O Delta é um projeto acadêmico do Ensino Médio Técnico em Análise e Desenvolvimento de Sistemas. O produto é
uma plataforma IoT para monitoramento inteligente do consumo residencial de água: sensores instalados em
hidrômetros enviam pulsos, o sistema consolida o consumo, identifica anomalias e disponibiliza informações para
aplicações web e mobile.

A arquitetura de dados do projeto é multi-banco:

- PostgreSQL mantém dados cadastrais e transacionais;
- MongoDB mantém telemetria IoT de alto volume e dados específicos da aplicação;
- Redis e Neo4j aparecem na documentação de arquitetura como componentes planejados, mas não devem ser
  tratados como implementados sem evidência nos repositórios atuais.

Os componentes do Delta são mantidos em repositórios Git independentes da organização `delta-app-ofc`. Faça
branches, commits e comandos Git dentro deste repositório; não inicialize Git na pasta agregadora
`Repositorios/` e não presuma a estrutura interna de outros serviços.

## 2. Contexto deste repositório

O `delta-api-postgres` é a API REST responsável por acessar e gerenciar dados relacionais do Projeto Delta no
PostgreSQL. A implementação atual usa:

- Java 17;
- Spring Boot 3.5.16;
- Spring Web para os endpoints REST;
- Spring Data JPA e Hibernate para persistência;
- Bean Validation para validar requisições;
- driver JDBC do PostgreSQL;
- Maven e Maven Wrapper para build e dependências;
- Lombok para reduzir código repetitivo;
- `spring-dotenv` para suporte à configuração por ambiente;
- JUnit e Spring Boot Test na estrutura de testes.

O escopo funcional confirmado na `main` é o CRUD de dispositivos no caminho `/delta/devices`:

| Método | Caminho | Responsabilidade |
| --- | --- | --- |
| `POST` | `/delta/devices` | Cadastrar dispositivo |
| `GET` | `/delta/devices` | Listar dispositivos |
| `GET` | `/delta/devices/{id}` | Consultar dispositivo por ID |
| `PUT` | `/delta/devices/{id}` | Atualizar dispositivo |
| `DELETE` | `/delta/devices/{id}` | Remover dispositivo |

O fluxo atual separa controller, DTOs, mapper, service, repositories e entidades. `Device` mapeia `tb_device` e
possui uma relação `ManyToOne` com `Property`, que mapeia `tb_property`. Existe um `PropertyRepository` para
validar e carregar a propriedade vinculada, mas não existem controller ou service de propriedade neste
repositório no estado atual.

O tratamento global de exceções padroniza respostas para recursos não encontrados, conflitos, erros de
validação, violações de integridade e falhas genéricas. Não amplie essa lista nem afirme outros comportamentos
sem verificar o código real.

## 3. Leitura obrigatória do `TASK.md`

Antes de executar qualquer tarefa:

1. Leia este `AGENTS.md` por completo.
2. Localize e leia integralmente o `TASK.md` na raiz deste repositório.
3. Verifique o estado do Git e inspecione os arquivos afetados na `main` atual.
4. Restrinja as alterações ao escopo definido no `TASK.md` e nas instruções do solicitante.

Se o `TASK.md` não existir, **não o crie** e não invente requisitos para substituí-lo. Informe a ausência ao
responsável e solicite o escopo necessário antes de prosseguir.

## 4. Estrutura atual do repositório

```text
delta-api-postgres/
├── .github/
│   └── workflows/
│       └── trigger_actions.yml
├── .mvn/
│   └── wrapper/
├── src/
│   ├── main/
│   │   ├── java/br/com/delta/delta_api_postgres/
│   │   │   ├── common/
│   │   │   │   ├── dto/
│   │   │   │   ├── exception/
│   │   │   │   └── handler/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   │   ├── io/
│   │   │   │   ├── params/
│   │   │   │   └── request/
│   │   │   ├── entity/
│   │   │   ├── enums/
│   │   │   ├── mapper/
│   │   │   ├── repository/
│   │   │   ├── service/
│   │   │   └── DeltaApiPostgresApplication.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/br/com/delta/delta_api_postgres/
│           └── DeltaApiPostgresApplicationTests.java
├── .env.example
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

Responsabilidades principais:

- `controller/`: contrato HTTP da API;
- `dto/`: dados recebidos, transportados e devolvidos pela aplicação;
- `mapper/`: conversão entre DTOs e entidades;
- `service/`: regras do fluxo de dispositivos e limites transacionais;
- `repository/`: acesso aos dados por Spring Data JPA;
- `entity/`: mapeamento das tabelas PostgreSQL;
- `common/`: formato de erro, exceções próprias e tratamento global;
- `application.properties`: configuração do datasource e do JPA;
- `.env.example`: nomes de variáveis de conexão com valores apenas ilustrativos;
- `trigger_actions.yml`: chamada ao workflow reutilizável da organização para verificações de Pull Request.

Há arquivos de backup com sufixo `.java~` versionados na árvore atual. Eles não fazem parte da estrutura de
pacotes compilada pelo Maven; não os remova nem os trate como fonte oficial sem uma tarefa explícita.

## 5. Configuração, banco e testes

O arquivo `application.properties` declara o driver e o dialeto PostgreSQL, mantém `ddl-auto=none` e deixa URL,
usuário e senha do datasource sem valores versionados. A `.env.example` contém somente placeholders. Nunca
inclua credenciais reais, URLs privadas ou segredos em commits.

Como `ddl-auto=none`, esta API não deve ser descrita como responsável por criar ou migrar o schema. Antes de
alterar entidades ou consultas, confirme o schema vigente no repositório de banco de dados correspondente e
as instruções do `TASK.md`.

A suíte atual contém apenas `contextLoads()` com `@SpringBootTest`. O workflow
`.github/workflows/trigger_actions.yml` chama as verificações reutilizáveis de Pull Request da organização e
não possui uma etapa Maven de testes no arquivo atual. Não declare cobertura, integração com banco ou
comportamento validado apenas porque o projeto compila ou porque esse teste existe.

## 6. Regras de arquitetura e escopo

- Não invente endpoints, entidades, tabelas, campos, bibliotecas, integrações ou camadas.
- Preserve a separação atual entre controller, DTO, mapper, service, repository e entity.
- Mantenha regras de negócio e transações na camada de serviço; controllers devem continuar focados no HTTP.
- Valide nomes e tipos de entidades contra o schema PostgreSQL real antes de modificá-los.
- Não habilite criação automática de schema nem altere a estratégia JPA sem solicitação explícita.
- Não crie migrations, Docker, autenticação, documentação OpenAPI ou novos workflows por suposição.
- Não remova arquivos, inclusive backups versionados, sem autorização específica.
- Diferencie configuração disponível, exemplo de configuração e conexão realmente validada.
- Limite cada tarefa aos arquivos necessários e preserve mudanças locais que pertençam a outras pessoas.

## 7. Padrão de branches e commits

Siga `delta-handbook/DEVOPS/convencoes-desenvolvimento.md`.

Branches usam o formato:

```text
<tipo>/<descricao-da-alteracao>
```

Tipos permitidos:

| Tipo | Uso |
| --- | --- |
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `refactor` | Refatoração sem nova funcionalidade |
| `docs` | Alteração de documentação |
| `test` | Criação ou manutenção de testes |
| `style` | Alteração de estilização |

Use descrição curta, clara, em minúsculas e separada por hífens, por exemplo `docs/agents-md`. Crie a branch no
próprio `delta-api-postgres`, sempre a partir da `main` atualizada, e nunca na raiz agregadora do workspace.

Os commits seguem Conventional Commits no formato `<tipo>: descrição`, usando os mesmos tipos permitidos. Não
misture mudanças sem relação no mesmo commit.

## 8. Padrão de documentação

Siga o padrão definido no `README.md` do `delta-handbook`:

- use Markdown (`.md`);
- comece com um título claro e apresente o objetivo do documento;
- registre contexto e justificativas para decisões técnicas;
- organize o conteúdo com seções e subseções descritivas;
- use listas, tabelas e blocos de código quando tornarem a informação mais verificável;
- inclua diagramas somente quando forem necessários para explicar relações ou fluxos;
- mantenha o histórico de atualizações relevantes quando aplicável;
- nomeie novos arquivos em minúsculas e com palavras separadas por hífens, como `configuracao-local.md`;
- confira se já existe documentação equivalente antes de criar outro arquivo;
- mantenha comandos e exemplos coerentes com a configuração e o código atuais.

Em documentação técnica, diferencie explicitamente o que está implementado, o que é exemplo, o que está
planejado e o que foi realmente validado. Não transforme suposições em fatos.

## 9. Limite de complexidade e nível técnico

As soluções devem ser compatíveis com o conhecimento de estudantes do Ensino Médio Técnico em Análise e Desenvolvimento de Sistemas.

- Priorize código simples, legível e dividido em pequenas responsabilidades.
- Utilize primeiro os recursos já presentes no repositório e conhecidos pela equipe.
- Não adicione frameworks, bibliotecas, padrões arquiteturais ou infraestrutura sem necessidade comprovada.
- Evite abstrações prematuras, metaprogramação, arquiteturas distribuídas e padrões avançados quando uma solução direta atender ao requisito.
- Não reestruture grandes partes do projeto para resolver uma tarefa localizada.
- Explique decisões técnicas e trechos não óbvios com linguagem didática.
- Quando a solução exigir conhecimento acima do limite registrado abaixo, apresente primeiro uma alternativa mais simples e solicite aprovação antes de prosseguir.
- Não implemente automaticamente uma solução avançada sem justificativa e autorização explícita.

### Stack e nível de aprofundamento da equipe

| Tecnologia ou assunto | Nível atual | Limite esperado |
| --- | --- | --- |
| Lógica de programação | Intermediário | Avançado |
| Git e GitHub | Intermediário | Avançado |
| HTML e CSS | Básico | Intermediário |
| JavaScript | Básico | Intermediário |
| Java | Intermediário | Avançado |
| Spring Boot | Básico | Avançado |
| Python | Intermediário | Avançado |
| FastAPI | Básico | Intermediário |
| SQL e PostgreSQL | Avançado | Avançado |
| MongoDB | Básico | Intermediário |
| APIs REST | Intermediário | Intermediário |
| Testes automatizados | Básico | Intermediário |
| Docker e CI/CD | Básico | Intermediário |
| Arquitetura e padrões de projeto | Básico | Intermediário |
| IoT e comunicação com hardware | Básico | Básico |

O **nível atual** representa o conhecimento que a equipe já possui e consegue aplicar com alguma autonomia. O **limite esperado** representa o nível máximo de complexidade que a IA pode utilizar.

Quando o limite esperado for superior ao nível atual, a IA deve explicar os novos conceitos de forma simples e didática, relacionando-os ao código produzido. Qualquer solução que ultrapasse o limite esperado exige aprovação explícita antes da implementação.

## 10. Aviso de manutenção

A seção **Estrutura atual do repositório** deve ser revisada periodicamente nesta conversa e atualizada depois
de commits oficiais que adicionem, removam ou reorganizem arquivos. Antes de cada atualização, compare esta
descrição com a árvore real da `main`; o conteúdo deste arquivo não substitui a inspeção do estado atual.
