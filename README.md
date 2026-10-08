# delta-api-postgres
Spring Boot REST API responsável por gerenciar dados relacionais na plataforma Delta.
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
