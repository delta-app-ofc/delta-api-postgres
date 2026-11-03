# Delta · API relacional PostgreSQL

![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.16-6DB33F?logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Spring%20Data%20JPA-4169E1?logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-refresh%20tokens-DC382D?logo=redis&logoColor=white)
![OpenAPI](https://img.shields.io/badge/springdoc-2.8.17-6BA539?logo=swagger&logoColor=white)

A API PostgreSQL centraliza autenticação, cadastro e operações relacionais do Delta sobre usuários, imóveis, dispositivos, tarifas e consumo. PostgreSQL armazena os dados relacionais e Redis mantém as sessões de refresh token.

**Tags:** `delta` · `api-rest` · `spring-boot` · `java-17` · `postgresql` · `redis` · `jwt-rs256` · `openapi`

## Sumário

- [Visão geral](#visão-geral)
- [Arquitetura e módulos](#arquitetura-e-módulos)
- [Configuração e execução](#configuração-e-execução)
- [Testes e build de container](#testes-e-build-de-container)
- [Cadastro mobile em duas chamadas](#cadastro-mobile-em-duas-chamadas)
- [Consulta pública de classificação por e-mail](#consulta-pública-de-classificação-por-e-mail)
- [Consulta, atualização e exclusão do usuário](#consulta-atualização-e-exclusão-do-usuário)
- [Estrutura do repositório](#estrutura-do-repositório)
- [Repositórios relacionados](#repositórios-relacionados)

## Visão geral

| Camada | Responsabilidade |
|---|---|
| Autenticação | Cadastro e login, access tokens JWT assinados com RSA, renovação de sessão e controle do próprio perfil. |
| Dados relacionais | Operações sobre usuários, endereços, imóveis, dispositivos, hábitos, organizações, tarifas e contas de água. |
| Autorização | Vincula a identidade autenticada às consultas e alterações de dados; respostas de erro usam um formato comum. |
| Sessões | Usa Redis para refresh tokens; a conexão é configurável por ambiente. |
| Analytics | Consultas de consumo, custo, rankings, variação e auditoria. |
| Documentação | Expõe Swagger UI e especificação OpenAPI. |

A API usa o schema PostgreSQL definido em [delta-sql-database](https://github.com/delta-app-ofc/delta-sql-database); o Hibernate está configurado com `ddl-auto=none`. PostgreSQL e Redis são dependências externas e devem estar acessíveis à aplicação.

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

No Windows PowerShell, use `.\mvnw.cmd test`. A suíte usa H2 em memória no modo PostgreSQL e simula o Redis.

O Dockerfile gera a imagem de execução na porta `10000` por padrão. Para criar a imagem:

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

Chamadas repetidas com os mesmos dados retornam os registros existentes, e a rota serializa solicitações simultâneas para o mesmo usuário. Imóveis ou hábitos diferentes geram `409`; atualize esses dados pelas rotas específicas.

O cadastro do imóvel chama a procedure `sp_register_property` na mesma transação que cria endereço, imóvel, associação e hábitos.

## Consulta pública de classificação por e-mail

`GET /delta/auth/classification?email=davi%40example.com` dispensa autenticação e retorna somente:

```json
{ "classificationGroup": "RESIDENCIAL" }
```

O valor pode ser `RESIDENCIAL` ou `COMERCIAL` e vem do grupo de classificação dos imóveis vinculados ao usuário ativo, não do tipo CASA/PRÉDIO. O e-mail é obrigatório, validado e normalizado para minúsculas sem espaços externos. A resposta usa `Cache-Control: no-store`.

Retorna `400` para e-mail ausente/inválido, `404` para usuário inexistente, inativo ou sem imóvel vinculado e `409` quando há imóveis dos dois grupos. Vários imóveis do mesmo grupo retornam uma única classificação.

## Consulta, atualização e exclusão do usuário

As rotas abaixo usam `Authorization: Bearer <accessToken>`. A API identifica o titular do perfil pelo token da sessão.

| Método e rota | Comportamento |
| --- | --- |
| `GET /delta/auth/me` | Retorna `id`, `name`, `email`, `phone` e `birthDate`, inclusive para cadastro ainda incompleto. |
| `PUT /delta/auth/me` | Atualiza nome, e-mail, telefone e nascimento; retorna o perfil atualizado. |
| `DELETE /delta/auth/me` | Desativa a conta (`is_active = false`), com resposta `204`. |
| `GET /delta/address`, `/delta/property`, `/delta/device`, `/delta/last-water-bills` | Lista os registros do usuário autenticado. |
| `GET`, `PUT`, `DELETE` nas rotas anteriores com `/{id}` | Consulta, altera ou exclui o registro após verificar titularidade e dependências. |
| `GET /delta/habits/{userId}` e `/delta/user-properties/{userId}` | Lista hábitos e vínculos do próprio usuário. GET, PUT e DELETE com `/{id}` gerenciam um registro específico. |
| `GET /delta/organizations` e `/{id}` | Consulta organizações relacionadas aos imóveis do usuário. |

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

DELETE desativa a conta (`is_active = false`) e preserva dados pessoais, hábitos, contas, vínculos e recursos compartilhados. A autenticação verifica o estado ativo no PostgreSQL; os refresh tokens armazenados no Redis seguem seu TTL, e o e-mail permanece reservado.

Endereços são associados aos imóveis por `tb_user_property`. O cadastro inicial usa `complete-registration`, que cria o endereço, o imóvel e o vínculo numa transação. Os campos do endereço são `regionId`, `cep`, `city`, `state`, `latitude` e `longitude`.

Usuários vinculados a um imóvel consultam os dados compartilhados. Alterações que afetem outro usuário retornam `409`. A exclusão de vínculo remove a associação do solicitante; a associação inicial é criada junto com o imóvel.

A exclusão de endereço retorna `409` enquanto houver imóvel associado. Para excluir um imóvel, remova primeiro seus dispositivos; quando o imóvel exclusivo não tiver dispositivos, a API remove seus vínculos na mesma transação e preserva o endereço. A exclusão de dispositivo remove o cadastro SQL, e `deviceId` permanece imutável. Hábitos e contas de água são gerenciados pelo próprio titular.

A API lista organizações relacionadas aos imóveis do usuário. Os catálogos de regiões, tarifas, classificações, hábitos e dias são compartilhados pelo sistema.

As consultas de imóvel e custo de água verificam o vínculo com o usuário. Os rankings residenciais e de imóveis usam o perfil autenticado para selecionar os resultados. A comparação de cenários de investimento também está disponível na área de analytics.

Erros seguem `ErrorResponse`: `400` para dados inválidos, `401` para sessão ausente/inválida ou conta inativa, `403` para tentativa explícita de usar outro usuário ou operação sem permissão, `404` para registro inexistente/fora do escopo e `409` para duplicidade, compartilhamento ou dependência de exclusão.

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
