# Delta Â· API relacional PostgreSQL

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Spring%20Data%20JPA-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-refresh%20tokens-DC382D?logo=redis&logoColor=white)
![OpenAPI](https://img.shields.io/badge/springdoc-2.8.17-6BA539?logo=swagger&logoColor=white)

A API PostgreSQL centraliza autenticaÃ§Ã£o, cadastro e operaÃ§Ãµes relacionais do Delta sobre usuÃ¡rios, imÃ³veis, dispositivos, tarifas e consumo. PostgreSQL armazena os dados relacionais e Redis mantÃ©m as sessÃµes de refresh token.

**Tags:** `delta` Â· `api-rest` Â· `spring-boot` Â· `java-17` Â· `postgresql` Â· `redis` Â· `jwt-rs256` Â· `openapi`

## SumÃ¡rio

- [VisÃ£o geral](#visÃ£o-geral)
- [Arquitetura e mÃ³dulos](#arquitetura-e-mÃ³dulos)
- [ConfiguraÃ§Ã£o e execuÃ§Ã£o](#configuraÃ§Ã£o-e-execuÃ§Ã£o)
- [Testes e build de container](#testes-e-build-de-container)
- [Cadastro mobile em duas chamadas](#cadastro-mobile-em-duas-chamadas)
- [Consulta pÃºblica de classificaÃ§Ã£o por e-mail](#consulta-pÃºblica-de-classificaÃ§Ã£o-por-e-mail)
- [Consulta, atualizaÃ§Ã£o e exclusÃ£o do usuÃ¡rio](#consulta-atualizaÃ§Ã£o-e-exclusÃ£o-do-usuÃ¡rio)
- [Estrutura do repositÃ³rio](#estrutura-do-repositÃ³rio)
- [RepositÃ³rios relacionados](#repositÃ³rios-relacionados)

## VisÃ£o geral

| Camada | Responsabilidade |
|---|---|
| AutenticaÃ§Ã£o | Cadastro e login, access tokens JWT assinados com RSA, renovaÃ§Ã£o de sessÃ£o e controle do prÃ³prio perfil. |
| Dados relacionais | OperaÃ§Ãµes sobre usuÃ¡rios, endereÃ§os, imÃ³veis, dispositivos, hÃ¡bitos, organizaÃ§Ãµes, tarifas e contas de Ã¡gua. |
| AutorizaÃ§Ã£o | Vincula a identidade autenticada Ã s consultas e alteraÃ§Ãµes de dados; respostas de erro usam um formato comum. |
| SessÃµes | Usa Redis para refresh tokens; a conexÃ£o Ã© configurÃ¡vel por ambiente. |
| Analytics | Consultas de consumo, custo, rankings, variaÃ§Ã£o e auditoria. |
| DocumentaÃ§Ã£o | ExpÃµe Swagger UI e especificaÃ§Ã£o OpenAPI. |

A API usa o schema PostgreSQL definido em [delta-sql-database](https://github.com/delta-app-ofc/delta-sql-database); o Hibernate estÃ¡ configurado com `ddl-auto=none`. PostgreSQL e Redis sÃ£o dependÃªncias externas e devem estar acessÃ­veis Ã  aplicaÃ§Ã£o.

## Arquitetura e mÃ³dulos

A aplicaÃ§Ã£o organiza os endpoints por mÃ³dulos em `src/main/java/br/com/delta/delta_api_postgres/modules/`. Cada mÃ³dulo mantÃ©m controllers, serviÃ§os, entidades, repositÃ³rios, DTOs, mapeadores e anotaÃ§Ãµes OpenAPI conforme necessÃ¡rio.

| MÃ³dulo | DomÃ­nio |
|---|---|
| `auth` | Cadastro, login, refresh token, perfil e autorizaÃ§Ã£o JWT. |
| `address`, `property`, `user_property` | EndereÃ§os, imÃ³veis e vÃ­nculos entre usuÃ¡rios e imÃ³veis. |
| `device`, `last_water_bill`, `habit` | Dispositivos, contas de Ã¡gua e hÃ¡bitos de consumo. |
| `organization`, `region_rate` | OrganizaÃ§Ãµes, regiÃµes e tarifas. |
| `analytics` | Consultas analÃ­ticas e rankings expostos pela API. |
| `common` | SaÃºde, configuraÃ§Ã£o OpenAPI, tratamento de erros e serviÃ§os compartilhados. |

O endpoint `GET /health` verifica PostgreSQL e Redis. Swagger UI fica em `/swagger-ui.html` e o JSON OpenAPI em `/v3/api-docs`. O servidor local usa a porta padrÃ£o do Spring Boot, `8080`; o Dockerfile configura a imagem de execuÃ§Ã£o para a porta `10000`, ajustÃ¡vel por `PORT`.

## ConfiguraÃ§Ã£o e execuÃ§Ã£o

Requisitos: Java 17 e o Maven Wrapper versionado neste repositÃ³rio. Para execuÃ§Ã£o funcional tambÃ©m sÃ£o necessÃ¡rios um PostgreSQL com o schema Delta jÃ¡ instalado e um Redis acessÃ­vel. A aplicaÃ§Ã£o lÃª `.env` no desenvolvimento local.

1. Copie `.env.example` para `.env` e configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` e a conexÃ£o Redis conforme seu ambiente. O arquivo de exemplo contÃ©m valores ilustrativos; nÃ£o versione credenciais reais.
2. Na primeira execuÃ§Ã£o local, gere um par RSA de desenvolvimento. O utilitÃ¡rio nÃ£o sobrescreve arquivos existentes; as chaves ficam em `.local/`, ignorada pelo Git.

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

A configuraÃ§Ã£o JWT permite ajustar emissor, audiÃªncia, identificador da chave, origens CORS, limite de tentativas e validade do refresh token. O modo TLS do Redis tambÃ©m Ã© configurÃ¡vel; confira [`.env.example`](.env.example) e `src/main/resources/application.properties` para todos os nomes e padrÃµes.

## Testes e build de container

Execute os testes com o Maven Wrapper:

~~~bash
./mvnw test
~~~

No Windows PowerShell, use `.\mvnw.cmd test`. A suÃ­te usa H2 em memÃ³ria no modo PostgreSQL e simula o Redis.

O Dockerfile gera a imagem de execuÃ§Ã£o na porta `10000` por padrÃ£o. Para criar a imagem:

~~~bash
docker build -t delta-api-postgres:local .
~~~

## Cadastro mobile em duas chamadas

1. `POST /delta/auth/create-account` (pÃºblico, com limite de tentativas): recebe os dados das duas primeiras telas e retorna `accessToken`, `refreshToken` e seus prazos.

```json
{
  "name": "Davi",
  "email": "davi@example.com",
  "password": "senha1234",
  "phone": "11999999999",
  "birthDate": "2000-01-01"
}
```

2. `POST /delta/auth/complete-registration`, com `Authorization: Bearer <accessToken>`, recebe o complemento. Aguarde o sucesso da primeira chamada antes de enviar a segunda. O usuÃ¡rio vem exclusivamente do token; nÃ£o envie `userId` ou `addressId`.

```json
{
  "address": { "regionId": 1, "cep": "01001000", "city": "SÃ£o Paulo", "state": "SP" },
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

Os IDs de regiÃ£o, hÃ¡bito e dias devem existir no banco; os nÃºmeros acima sÃ£o exemplos. A consulta de CEP no aplicativo fornece cidade e estado, mas tambÃ©m Ã© necessÃ¡rio enviar a regiÃ£o cadastrada. `habits` pode ser uma lista vazia. Nome, tipo e classificaÃ§Ã£o do imÃ³vel sÃ£o obrigatÃ³rios; organizaÃ§Ã£o e Ã¡rea sÃ£o opcionais.

A segunda chamada responde `200` com `userId`, `completed: true`, `address`, `property` e `habits`, somente depois do commit. EndereÃ§o, imÃ³vel, associaÃ§Ã£o e hÃ¡bitos sÃ£o gravados numa transaÃ§Ã£o. A conta criada na primeira chamada permanece caso o complemento falhe.

Chamadas repetidas com os mesmos dados retornam os registros existentes, e a rota serializa solicitaÃ§Ãµes simultÃ¢neas para o mesmo usuÃ¡rio. ImÃ³veis ou hÃ¡bitos diferentes geram `409`; atualize esses dados pelas rotas especÃ­ficas.

O cadastro do imÃ³vel chama a procedure `sp_register_property` na mesma transaÃ§Ã£o que cria endereÃ§o, imÃ³vel, associaÃ§Ã£o e hÃ¡bitos.

## Consulta pÃºblica de classificaÃ§Ã£o por e-mail

`GET /delta/auth/classification?email=davi%40example.com` dispensa autenticaÃ§Ã£o e retorna somente:

```json
{ "classificationGroup": "RESIDENCIAL" }
```

O valor pode ser `RESIDENCIAL` ou `COMERCIAL` e vem do grupo de classificaÃ§Ã£o dos imÃ³veis vinculados ao usuÃ¡rio ativo, nÃ£o do tipo CASA/PRÃ‰DIO. O e-mail Ã© obrigatÃ³rio, validado e normalizado para minÃºsculas sem espaÃ§os externos. A resposta usa `Cache-Control: no-store`.

Retorna `400` para e-mail ausente/invÃ¡lido, `404` para usuÃ¡rio inexistente, inativo ou sem imÃ³vel vinculado e `409` quando hÃ¡ imÃ³veis dos dois grupos. VÃ¡rios imÃ³veis do mesmo grupo retornam uma Ãºnica classificaÃ§Ã£o.

## Consulta, atualizaÃ§Ã£o e exclusÃ£o do usuÃ¡rio

As rotas abaixo usam `Authorization: Bearer <accessToken>`. A API identifica o titular do perfil pelo token da sessÃ£o.

| MÃ©todo e rota | Comportamento |
| --- | --- |
| `GET /delta/auth/me` | Retorna `id`, `name`, `email`, `phone` e `birthDate`, inclusive para cadastro ainda incompleto. |
| `PUT /delta/auth/me` | Atualiza nome, e-mail, telefone e nascimento; retorna o perfil atualizado. |
| `DELETE /delta/auth/me` | Desativa a conta (`is_active = false`), com resposta `204`. |
| `GET /delta/address`, `/delta/property`, `/delta/device`, `/delta/last-water-bills` | Lista os registros do usuÃ¡rio autenticado. |
| `GET`, `PUT`, `DELETE` nas rotas anteriores com `/{id}` | Consulta, altera ou exclui o registro apÃ³s verificar titularidade e dependÃªncias. |
| `GET /delta/habits/{userId}` e `/delta/user-properties/{userId}` | Lista hÃ¡bitos e vÃ­nculos do prÃ³prio usuÃ¡rio. GET, PUT e DELETE com `/{id}` gerenciam um registro especÃ­fico. |
| `GET /delta/organizations` e `/{id}` | Consulta organizaÃ§Ãµes relacionadas aos imÃ³veis do usuÃ¡rio. |

Exemplo de PUT completo do perfil:

```json
{
  "name": "Davi Costa",
  "email": "davi@example.com",
  "phone": "11999999999",
  "birthDate": "2000-01-01"
}
```

Nome, e-mail e nascimento sÃ£o obrigatÃ³rios no PUT; `phone: null` (ou vazio) limpa o telefone. Nome aceita atÃ© 100 caracteres, e-mail atÃ© 255, telefone atÃ© 15 e nascimento deve ser anterior Ã  data atual. Nome e telefone tÃªm espaÃ§os externos removidos; e-mail Ã© normalizado para minÃºsculas e deve continuar Ãºnico. Senha, ID e estado de ativaÃ§Ã£o nÃ£o sÃ£o campos editÃ¡veis do perfil. Hash e tokens nÃ£o aparecem nas respostas. GET e PUT retornam `200`, com `Cache-Control: no-store`.

DELETE desativa a conta (`is_active = false`) e preserva dados pessoais, hÃ¡bitos, contas, vÃ­nculos e recursos compartilhados. A autenticaÃ§Ã£o verifica o estado ativo no PostgreSQL; os refresh tokens armazenados no Redis seguem seu TTL, e o e-mail permanece reservado.

EndereÃ§os sÃ£o associados aos imÃ³veis por `tb_user_property`. O cadastro inicial usa `complete-registration`, que cria o endereÃ§o, o imÃ³vel e o vÃ­nculo numa transaÃ§Ã£o. Os campos do endereÃ§o sÃ£o `regionId`, `cep`, `city`, `state`, `latitude` e `longitude`.

UsuÃ¡rios vinculados a um imÃ³vel consultam os dados compartilhados. AlteraÃ§Ãµes que afetem outro usuÃ¡rio retornam `409`. A exclusÃ£o de vÃ­nculo remove a associaÃ§Ã£o do solicitante; a associaÃ§Ã£o inicial Ã© criada junto com o imÃ³vel.

A exclusÃ£o de endereÃ§o retorna `409` enquanto houver imÃ³vel associado. Para excluir um imÃ³vel, remova primeiro seus dispositivos; quando o imÃ³vel exclusivo nÃ£o tiver dispositivos, a API remove seus vÃ­nculos na mesma transaÃ§Ã£o e preserva o endereÃ§o. A exclusÃ£o de dispositivo remove o cadastro SQL, e `deviceId` permanece imutÃ¡vel. HÃ¡bitos e contas de Ã¡gua sÃ£o gerenciados pelo prÃ³prio titular.

A API lista organizaÃ§Ãµes relacionadas aos imÃ³veis do usuÃ¡rio. Os catÃ¡logos de regiÃµes, tarifas, classificaÃ§Ãµes, hÃ¡bitos e dias sÃ£o compartilhados pelo sistema.

As consultas de imÃ³vel e custo de Ã¡gua verificam o vÃ­nculo com o usuÃ¡rio. Os rankings residenciais e de imÃ³veis usam o perfil autenticado para selecionar os resultados. A comparaÃ§Ã£o de cenÃ¡rios de investimento tambÃ©m estÃ¡ disponÃ­vel na Ã¡rea de analytics.

Erros seguem `ErrorResponse`: `400` para dados invÃ¡lidos, `401` para sessÃ£o ausente/invÃ¡lida ou conta inativa, `403` para tentativa explÃ­cita de usar outro usuÃ¡rio ou operaÃ§Ã£o sem permissÃ£o, `404` para registro inexistente/fora do escopo e `409` para duplicidade, compartilhamento ou dependÃªncia de exclusÃ£o.

## Estrutura do repositÃ³rio

| Caminho | ConteÃºdo |
|---|---|
| `src/main/java/.../modules/` | MÃ³dulos HTTP, regras da aplicaÃ§Ã£o, entidades e repositÃ³rios. |
| `src/main/java/.../common/` | ConfiguraÃ§Ã£o de seguranÃ§a/OpenAPI, health check, erros e serviÃ§os comuns. |
| `src/main/resources/application.properties` | ConfiguraÃ§Ã£o do datasource, JWT, Redis e documentaÃ§Ã£o HTTP. |
| `src/test/java/` e `src/test/resources/` | Testes automatizados com configuraÃ§Ã£o H2 isolada. |
| `scripts/GenerateLocalKeys.java` | GeraÃ§Ã£o local de chaves RSA de desenvolvimento, sem sobrescrever chaves existentes. |
| `.env.example` | Nomes e exemplos de configuraÃ§Ã£o; nÃ£o contÃ©m credenciais de produÃ§Ã£o. |
| `Dockerfile` | Build da aplicaÃ§Ã£o e imagem JRE de execuÃ§Ã£o. |
| `.github/workflows/` | AutomaÃ§Ã£o de Pull Requests existente. |

## RepositÃ³rios relacionados

- [delta-sql-database](https://github.com/delta-app-ofc/delta-sql-database): schema, procedures e demais artefatos PostgreSQL usados pela API.
- [delta-api-mongo](https://github.com/delta-app-ofc/delta-api-mongo): API de telemetria MongoDB.
- [delta-business-rules](https://github.com/delta-app-ofc/delta-business-rules): processos determinÃ­sticos de consumo, alertas e tarifas.
- [delta-handbook](https://github.com/delta-app-ofc/delta-handbook): documentaÃ§Ã£o de arquitetura e regras do projeto.
