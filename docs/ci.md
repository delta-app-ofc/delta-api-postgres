# CI técnico

O workflow `CI técnico` executa em pull requests e manualmente. Ele valida a configuração do Compose e executa a suíte Maven existente em um container Java 17. Uma falha no build ou nos testes falha o job.

Para executar localmente, com Docker Engine e Compose v2:

```bash
docker compose -f docker-compose.test.yml config --quiet
docker compose -f docker-compose.test.yml run --build --rm tests
docker compose -f docker-compose.test.yml down
```

Os testes usam H2 em memória, incluindo cenários HTTP e de serviços existentes. Eles não validam a compatibilidade integral com PostgreSQL, o schema e as procedures oficiais ou integrações externas. Consulte [DOCKER-HELP.md](../DOCKER-HELP.md) para detalhes e limitações.
