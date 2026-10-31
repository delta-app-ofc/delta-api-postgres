# Docker — delta-api-postgres

## Objetivo

Executar a suíte Maven existente em container. Os testes usam a configuração H2 em memória de `src/test/resources/application.properties`; não precisam de um servidor PostgreSQL ou de `.env`.

## Pré-requisitos

Docker Engine em execução e Docker Compose v2. Execute os comandos na raiz deste repositório.

## Executar

```bash
docker compose -p delta-api-postgres-test -f docker-compose.test.yml config --quiet
docker compose -p delta-api-postgres-test -f docker-compose.test.yml run --build --rm tests
docker compose -p delta-api-postgres-test -f docker-compose.test.yml down
```

Confira o resumo Maven e o código de saída do runner. O teste deve encerrar com `BUILD SUCCESS`, sem falhas ou erros. O runner não publica portas ou monta arquivos do host. Os testes que precisam de chaves de autenticação geram suas chaves de teste pela configuração existente.

A suíte cobre os cenários implementados de serviços, mapper, autenticação, validação de dispositivos e cadastro. O modo PostgreSQL do H2 não comprova compatibilidade integral com PostgreSQL nem execução das procedures e do schema oficial.

## Construir a imagem de execução

```bash
docker build -t delta-api-postgres:local .
```

O alvo padrão continua sendo a imagem de execução definida no Dockerfile da aplicação. Seu empacotamento não executa os testes; use o fluxo acima para validar a suíte.

A configuração H2 da suíte atual pode emitir warnings de geração de DDL. A aprovação dos testes não garante que todos os objetos do schema oficial tenham sido criados ou exercitados; essa verificação deve usar o PostgreSQL e os scripts oficiais.
