# Delta · API relacional PostgreSQL

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Spring%20Data%20JPA-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-refresh%20tokens-DC382D?logo=redis&logoColor=white)
![OpenAPI](https://img.shields.io/badge/springdoc-2.8.17-6BA539?logo=swagger&logoColor=white)

A API que protege a conta e conecta pessoas aos seus imóveis, dispositivos, tarifas e dados de consumo. Este serviço Spring Boot concentra autenticação, cadastro e operações relacionais do Delta; PostgreSQL guarda o estado cadastral, e Redis armazena as sessões de refresh token.

**Tags:** `delta` · `api-rest` · `spring-boot` · `java-17` · `postgresql` · `redis` · `jwt-rs256` · `openapi`

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura e módulos](#arquitetura-e-módulos)
- [Configuração e execução](#configuração-e-execução)
- [Testes e build de container](#testes-e-build-de-container)
- [Cadastro mobile em duas chamadas](#cadastro-mobile-em-duas-chamadas)
- [Banco indisponível por cota do Neon](#banco-indisponível-por-cota-do-neon)
- [Consulta pública de classificação por e-mail](#consulta-pública-de-classificação-por-e-mail)
- [Consulta, atualização e exclusão do usuário](#consulta-atualização-e-exclusão-do-usuário)
- [Testes e validação](#testes-e-validação)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Repositórios relacionados](#repositórios-relacionados)

## Visão geral

| Camada | Responsabilidade |
|---|---|
| Autenticação | Cadastro e login, access tokens JWT assinados com RSA, renovação de sessão e controle do próprio perfil. |
| Dados relacionais | Operações sobre usuários, endereços, imóveis, dispositivos, hábitos, organizações, tarifas e contas de água. |
| Autorização | Vincula a identidade autenticada às consultas e alterações de dados; respostas de erro usam um formato comum. |
| Sessões | Usa Redis para refresh tokens; a conexão é configurável por ambiente. |
| Analytics | Expõe consultas de consumo, custo, rankings, variação e auditoria, respeitando os escopos implementados. |
| Documentação | Expõe Swagger UI e especificação OpenAPI. |

O schema PostgreSQL não é criado pelo Hibernate (`ddl-auto=none`). Prepare as tabelas e rotinas necessárias a partir do repositório [delta-sql-database](https://github.com/delta-app-ofc/delta-sql-database) antes de conectar a API. A configuração local não provisiona PostgreSQL ou Redis.

## Arquitetura e módulos

A aplicação organiza os endpoints por módulos em `src/main/java/br/com/delta/delta_api_postgres/modules/`. Cada módulo mantém controllers, serviços, entidades, repositórios, DTOs, mapeadores e anotações OpenAPI conforme necessário.

| Módulo | Domínio |
|---|---|
| `auth` | Cadastro, login, refresh token, perfil e autorização JWT. |
| `address`, `property`, `user_property` | Endereços, imóveis e vínculos entre usuários e imóveis. |
| `device`, `last_water_bill`, `habit` | Dispositivos, contas de água e hábitos de consumo. |
| `organization`, `region_rate` | Organizações, regiões e tarifas. |
| `analytics` | Consultas analíticas e rankings expostos pela API. |
| `common` | Saúde, configuração OpenAPI, tratamento de erros e serviços compartilhados. |

O endpoint `GET /health` verifica PostgreSQL e Redis. Swagger UI fica em `/swagger-ui.html` e o JSON OpenAPI em `/v3/api-docs`. O servidor local usa a porta padrão do Spring Boot, `8080`; o Dockerfile configura a imagem de execução para a porta `10000`, ajustável por `PORT`.

## Configuração e execução

Requisitos: Java 17 e o Maven Wrapper versionado neste repositório. Para execução funcional também são necessários um PostgreSQL com o schema Delta já instalado e um Redis acessível. A aplicação lê `.env` no desenvolvimento local.

1. Copie `.env.example` para `.env` e configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e a conexão Redis conforme seu ambiente. O arquivo de exemplo contém valores ilustrativos; não versione credenciais reais.
2. Na primeira execução local, gere um par RSA de desenvolvimento. O utilitário não sobrescreve arquivos existentes; as chaves ficam em `.local/`, ignorada pelo Git.

Linux/macOS:

~~~bash
cp .env.example .env
mkdir -p .local
javac -d .local scripts/GenerateLocalKeys.java
java -cp .local GenerateLocalKeys
./mvnw spring-boot:run
~~~

Windows PowerShell:

~~~powershell
Copy-Item .env.example .env
New-Item -ItemType Directory -Force .local | Out-Null
javac -d .local scripts/GenerateLocalKeys.java
java -cp .local GenerateLocalKeys
.\mvnw.cmd spring-boot:run
~~~

A configuração JWT permite ajustar emissor, audiência, identificador da chave, origens CORS, limite de tentativas e validade do refresh token. O modo TLS do Redis também é configurável; confira [`.env.example`](.env.example) e `src/main/resources/application.properties` para todos os nomes e padrões.

## Testes e build de container

Execute os testes com o Maven Wrapper:

~~~bash
./mvnw test
~~~

No Windows PowerShell, use `.\mvnw.cmd test`. Os testes configuram H2 em memória no modo PostgreSQL e simulam o Redis quando necessário; eles não substituem validação das procedures, triggers e restrições no PostgreSQL real.

O Dockerfile constrói uma imagem de execução que escuta na porta `10000` por padrão. O estágio de build usa `-DskipTests`, então rode a suíte separadamente antes de gerar a imagem:

~~~bash
docker build -t delta-api-postgres:local .
~~~

## Cadastro mobile em duas chamadas

1. `POST /delta/auth/create-account` (público, com limite de tentativas): recebe os dados das duas primeiras telas e retorna `accessToken`, `refreshToken` e seus prazos.

```json
{
  "name": "Davi",
  "email": "davi@example.com",
  "password": "senha1234",
  "phone": "11999999999",
  "birthDate": "2000-01-01"
}
```

2. `POST /delta/auth/complete-registration`, com `Authorization: Bearer <accessToken>`, recebe o complemento. Aguarde o sucesso da primeira chamada antes de enviar a segunda. O usuário vem exclusivamente do token; não envie `userId` ou `addressId`.

```json
{
  "address": { "regionId": 1, "cep": "01001000", "city": "São Paulo", "state": "SP" },
  "property": {
    "name": "Casa principal",
    "type": "CASA",
    "classification": "RESIDENCIAL_NORMAL",
    "organizationId": null,
    "builtAreaM2": 80
  },
  "habits": [ { "habitId": 1, "frequency": 2, "daysOfWeek": [1, 3] } ]
}
```

Os IDs de região, hábito e dias devem existir no banco; os números acima são exemplos. A consulta de CEP no aplicativo fornece cidade e estado, mas também é necessário enviar a região cadastrada. `habits` pode ser uma lista vazia. Nome, tipo e classificação do imóvel são obrigatórios; organização e área são opcionais.

A segunda chamada responde `200` com `userId`, `completed: true`, `address`, `property` e `habits`, somente depois do commit. Endereço, imóvel, associação e hábitos são gravados numa transação. A conta criada na primeira chamada permanece caso o complemento falhe.

Reenvios com os mesmos dados persistidos retornam os registros existentes. Um bloqueio no usuário serializa as chamadas concorrentes dessa rota. Se o usuário já tiver imóvel ou hábitos diferentes, a rota responde `409`; alterações posteriores devem usar as rotas específicas. Isso não é um recibo permanente de idempotência: se os dados forem alterados depois, reenviar o cadastro antigo pode resultar em conflito.

A criação do imóvel utiliza a procedure existente `sp_register_property`; ela deve estar instalada no PostgreSQL e participar da transação, sem commit interno. A orquestração garante a associação com o usuário caso a procedure ainda não a crie. Não há chamadas HTTP internas para os controllers.

## Banco indisponível por cota do Neon

O erro PostgreSQL `53000` com a mensagem `Your account or project has exceeded the quota` significa que o provedor recusou a conexão por uma cota da conta ou do projeto. O Hikari tenta conectar e o Hibernate não consegue abrir a transação. A mensagem não identifica qual cota foi excedida.

No [painel do Neon](https://console.neon.tech), verifique o Usage do projeto e Billing da organização para identificar o limite e o período de consumo. A recuperação depende da cota: disponibilizar capacidade no plano, aguardar a renovação do período quando aplicável ou contatar o suporte em caso de divergência. Reiniciar a API ou alterar a senha não remove o bloqueio. Não é necessário recriar ou apagar tabelas.

A API responde `503` com `Banco de dados indisponível. Tente novamente mais tarde.` para falhas de abertura da transação ou indisponibilidade de conexão que chegam aos controllers. O endpoint `/health` também retorna `503` quando as dependências estão indisponíveis. Isso informa a falha ao cliente, mas não libera a cota do provedor.

O pool mantém zero conexões mínimas ociosas, encerra conexões ociosas após aproximadamente um minuto, desabilita keepalive e limita a cinco conexões por instância (`DB_POOL_MAX_SIZE` ajustável). Esses ajustes reduzem conexões e consultas de manutenção desnecessárias; não corrigem uma cota já esgotada. Monitores que consultam `/health` com frequência acessam PostgreSQL e Redis e podem manter o compute ativo. Revise a frequência desses monitores no ambiente de hospedagem. Reinicie a aplicação após atualizar a configuração.

## Consulta pública de classificação por e-mail

`GET /delta/auth/classification?email=davi%40example.com` dispensa autenticação e retorna somente:

```json
{ "classificationGroup": "RESIDENCIAL" }
```

O valor pode ser `RESIDENCIAL` ou `COMERCIAL` e vem do grupo de classificação dos imóveis vinculados ao usuário ativo, não do tipo CASA/PRÉDIO. O e-mail é obrigatório, validado e normalizado para minúsculas sem espaços externos. A resposta usa `Cache-Control: no-store`.

Retorna `400` para e-mail ausente/inválido, `404` para usuário inexistente, inativo ou sem imóvel vinculado e `409` quando há imóveis dos dois grupos. Vários imóveis do mesmo grupo retornam uma única classificação.

## Consulta, atualização e exclusão do usuário

Todas as rotas abaixo exigem `Authorization: Bearer <accessToken>`. O titular do perfil vem do token; IDs de usuário recebidos nas rotas legadas ou no corpo devem corresponder ao mesmo usuário.

| Método e rota | Comportamento |
| --- | --- |
| `GET /delta/auth/me` | Retorna `id`, `name`, `email`, `phone` e `birthDate`, inclusive para cadastro ainda incompleto. |
| `PUT /delta/auth/me` | Atualiza nome, e-mail, telefone e nascimento; retorna o perfil atualizado. |
| `DELETE /delta/auth/me` | Desativa a conta (`is_active = false`), com resposta `204`. |
| `GET /delta/address`, `/delta/property`, `/delta/device`, `/delta/last-water-bills` | Lista somente registros ligados ao usuário do token. |
| `GET`, `PUT`, `DELETE` nas rotas anteriores com `/{id}` | Consulta, altera ou exclui o registro após verificar titularidade e dependências. |
| `GET /delta/habits/{userId}` e `/delta/user-properties/{userId}` | Lista hábitos e vínculos do próprio usuário. GET, PUT e DELETE com `/{id}` gerenciam um registro específico. |
| `GET /delta/organizations` e `/{id}` | Consulta apenas organizações relacionadas aos imóveis do usuário. |

Exemplo de PUT completo do perfil:

```json
{
  "name": "Davi Costa",
  "email": "davi@example.com",
  "phone": "11999999999",
  "birthDate": "2000-01-01"
}
```

Nome, e-mail e nascimento são obrigatórios no PUT; `phone: null` (ou vazio) limpa o telefone. Nome aceita até 100 caracteres, e-mail até 255, telefone até 15 e nascimento deve ser anterior à data atual. Nome e telefone têm espaços externos removidos; e-mail é normalizado para minúsculas e deve continuar único. Senha, ID e estado de ativação não são campos editáveis do perfil. Hash e tokens não aparecem nas respostas. GET e PUT retornam `200`, com `Cache-Control: no-store`.

DELETE do perfil é **exclusão lógica**: conserva dados pessoais, hábitos, contas, vínculos e recursos compartilhados. Não equivale a apagar ou anonimizar dados. Login, access tokens e refresh tokens anteriores deixam de ser aceitos porque a autenticação verifica a conta ativa no PostgreSQL. As chaves de refresh no Redis expiram pelo TTL existente; a desativação não depende de limpeza do Redis e não há reativação pública da conta. O e-mail continua reservado.

Endereços pertencem aos imóveis, por meio dos vínculos de `tb_user_property`, e não diretamente ao usuário. O endereço atual suporta `regionId`, `cep`, `city`, `state`, `latitude` e `longitude` (as duas últimas são obrigatórias). Rua, número, bairro e complemento exigiriam alteração do schema e não são aceitos como campos persistidos. O POST de endereço legado cria um registro sem titularidade comprovável, pois a tabela não possui vínculo direto de proprietário. Esses registros não podem ser consultados nem adotados por simples conhecimento do ID. O CRUD público de imóvel só reutiliza endereços já vinculados exclusivamente ao usuário. Para o primeiro endereço/imóvel, use `complete-registration`, que cria ambos e garante o vínculo na mesma transação. O fluxo legado POST de endereço seguido de POST de imóvel com ID solto não é suportado pelas regras de titularidade; permitir esse fluxo exige persistir a autoria do endereço no schema.

Imóveis, endereços e dispositivos compartilhados podem ser consultados pelos usuários vinculados. Alterações e exclusões que afetem outro usuário retornam `409`, pois o modelo atual não define papéis de administrador. DELETE de vínculo remove somente o vínculo do solicitante. A associação arbitrária a imóvel alheio é recusada; o CRUD de vínculos não funciona como convite ou transferência de propriedade. A criação inicial do vínculo ocorre no cadastro do imóvel.

Endereço ainda referenciado por imóvel retorna `409` no DELETE. Imóvel com dispositivos precisa ter os dispositivos removidos antes da exclusão; ao excluir um imóvel exclusivo sem dispositivos, seus vínculos são removidos na mesma transação. O endereço é preservado. DELETE de dispositivo remove o cadastro SQL, sem apagar telemetria externa; `deviceId` continua imutável. Hábitos e contas só podem ser alterados/excluídos pelo próprio titular, sem transferência de usuário.

Organizações são dados compartilhados sem regra de administração no modelo atual. POST, PUT e DELETE de organização retornam `403` até existir uma política de gestão. Catálogos de regiões, tarifas, classificações, hábitos-base e dias não são removidos junto ao usuário.

Analytics por imóvel e custo de água validam o vínculo; ranking residencial valida o usuário e ranking de imóveis filtra os imóveis do titular. A distribuição global de consumo usa uma chave de DW sem mapeamento comprovado para o imóvel SQL, e o histórico global de auditoria expõe outros executores; essas duas consultas retornam `403` até existir um escopo seguro. Comparação de cenários de investimento continua disponível como referência não vinculada ao perfil.

Erros seguem `ErrorResponse`: `400` para dados inválidos, `401` para sessão ausente/inválida ou conta inativa, `403` para tentativa explícita de usar outro usuário ou operação sem permissão, `404` para registro inexistente/fora do escopo e `409` para duplicidade, compartilhamento ou dependência de exclusão.

### Testes e validação

Execute `./mvnw.cmd test` no Windows. Os testes de integração usam H2, JWT assinado e Redis simulado; cobrem perfil, desativação, isolamento entre usuários, recursos compartilhados e dependências. Procedures, triggers, FKs adicionais e concorrência específica do PostgreSQL precisam ser validados em um banco de testes com o schema real antes de produção.

O último resultado registrado antes desta revisão foi de 137 testes aprovados, incluindo 21 cenários novos de perfil e autorização. Naquele registro, 10 testes HTTP de dispositivos não iniciaram por falha de loopback do Java (`Unable to establish loopback connection`); a suíte completa ainda precisava ser repetida em ambiente com sockets locais funcionais. Este é um registro histórico do README, não uma execução realizada nesta atualização. Nenhum teste foi desabilitado.

## Estrutura do repositório

| Caminho | Conteúdo |
|---|---|
| `src/main/java/.../modules/` | Módulos HTTP, regras da aplicação, entidades e repositórios. |
| `src/main/java/.../common/` | Configuração de segurança/OpenAPI, health check, erros e serviços comuns. |
| `src/main/resources/application.properties` | Configuração do datasource, JWT, Redis e documentação HTTP. |
| `src/test/java/` e `src/test/resources/` | Testes automatizados com configuração H2 isolada. |
| `scripts/GenerateLocalKeys.java` | Geração local de chaves RSA de desenvolvimento, sem sobrescrever chaves existentes. |
| `.env.example` | Nomes e exemplos de configuração; não contém credenciais de produção. |
| `Dockerfile` | Build da aplicação e imagem JRE de execução. |
| `.github/workflows/` | Automação de Pull Requests existente. |

## Repositórios relacionados

- [delta-sql-database](https://github.com/delta-app-ofc/delta-sql-database): schema, procedures e demais artefatos PostgreSQL usados pela API.
- [delta-api-mongo](https://github.com/delta-app-ofc/delta-api-mongo): API de telemetria MongoDB.
- [delta-business-rules](https://github.com/delta-app-ofc/delta-business-rules): processos determinísticos de consumo, alertas e tarifas.
- [delta-handbook](https://github.com/delta-app-ofc/delta-handbook): documentação de arquitetura e regras do projeto.
