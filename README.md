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
  "address": { "cep": "01001000", "city": "São Paulo", "state": "SP" },
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

Endereços pertencem aos imóveis, por meio dos vínculos de `tb_user_property`, e não diretamente ao usuário. O endereço atual suporta `regionId`, `cep`, `city`, `state`, `latitude` e `longitude`. Rua, número, bairro e complemento exigiriam alteração do schema e não são aceitos como campos persistidos. O POST de endereço legado cria um registro sem titularidade comprovável, pois a tabela não possui vínculo direto de proprietário. Esses registros não podem ser consultados nem adotados por simples conhecimento do ID. O CRUD público de imóvel só reutiliza endereços já vinculados exclusivamente ao usuário. Para o primeiro endereço/imóvel, use `complete-registration`, que cria ambos e garante o vínculo na mesma transação. O fluxo legado POST de endereço seguido de POST de imóvel com ID solto não é suportado pelas regras de titularidade; permitir esse fluxo exige persistir a autoria do endereço no schema.

Imóveis, endereços e dispositivos compartilhados podem ser consultados pelos usuários vinculados. Alterações e exclusões que afetem outro usuário retornam `409`, pois o modelo atual não define papéis de administrador. DELETE de vínculo remove somente o vínculo do solicitante. A associação arbitrária a imóvel alheio é recusada; o CRUD de vínculos não funciona como convite ou transferência de propriedade. A criação inicial do vínculo ocorre no cadastro do imóvel.

Endereço ainda referenciado por imóvel retorna `409` no DELETE. Imóvel com dispositivos precisa ter os dispositivos removidos antes da exclusão; ao excluir um imóvel exclusivo sem dispositivos, seus vínculos são removidos na mesma transação. O endereço é preservado. DELETE de dispositivo remove o cadastro SQL, sem apagar telemetria externa; `deviceId` continua imutável. Hábitos e contas só podem ser alterados/excluídos pelo próprio titular, sem transferência de usuário.

Organizações são dados compartilhados sem regra de administração no modelo atual. POST, PUT e DELETE de organização retornam `403` até existir uma política de gestão. Catálogos de regiões, tarifas, classificações, hábitos-base e dias não são removidos junto ao usuário.

Analytics por imóvel e custo de água validam o vínculo; ranking residencial valida o usuário e ranking de imóveis filtra os imóveis do titular. A distribuição global de consumo usa uma chave de DW sem mapeamento comprovado para o imóvel SQL, e o histórico global de auditoria expõe outros executores; essas duas consultas retornam `403` até existir um escopo seguro. Comparação de cenários de investimento continua disponível como referência não vinculada ao perfil.

Erros seguem `ErrorResponse`: `400` para dados inválidos, `401` para sessão ausente/inválida ou conta inativa, `403` para tentativa explícita de usar outro usuário ou operação sem permissão, `404` para registro inexistente/fora do escopo e `409` para duplicidade, compartilhamento ou dependência de exclusão.

### Validação da implementação

Execute `./mvnw.cmd test` no Windows. Os testes de integração usam H2, JWT assinado e Redis simulado; cobrem perfil, desativação, isolamento entre usuários, recursos compartilhados e dependências. Procedures, triggers, FKs adicionais e concorrência específica do PostgreSQL precisam ser validados em um banco de testes com o schema real antes de produção.

Resultado da validação desta implementação: 137 testes aprovados, incluindo 21 novos cenários de perfil e autorização. Os 10 testes HTTP existentes de dispositivos tiveram erro de inicialização por loopback do Java neste ambiente (`Unable to establish loopback connection`); a suíte completa ainda precisa ser repetida em ambiente com sockets locais funcionais. Nenhum teste foi desabilitado.

Consulte [DOCKER-HELP.md](DOCKER-HELP.md) para executar a suíte local em Docker e construir a imagem da aplicação.
