# delta-api-postgres
Spring Boot REST API responsável por gerenciar dados relacionais na plataforma Delta.

## Autenticação

O módulo usa a tabela existente `tb_user`, JWT RS256 com validade de 15 minutos
e senhas Argon2id. Nenhuma tabela adicional é necessária. Como os tokens são
stateless, o logout é feito removendo o token no cliente; um usuário desativado
em `tb_user.is_active` deixa de acessar a API imediatamente.

### Configuração

1. Configure `DB_URL`, `DB_USERNAME` e `DB_PASSWORD` no ambiente ou `.env`.
2. Gere um par RSA e mantenha a chave privada fora do controle de versão:

```sh
mkdir .local
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out .local/private.pem
openssl pkey -in .local/private.pem -pubout -out .local/public.pem
```

3. Em desenvolvimento, esses caminhos locais já são os padrões. Para usar
   outros arquivos, configure `AUTH_PRIVATE_KEY=file:/caminho/private.pem` e
   `AUTH_PUBLIC_KEY=file:/caminho/public.pem`. Em produção use armazenamento
   protegido para a chave privada e HTTPS.
4. Configure `AUTH_ALLOWED_ORIGINS` com as origens do frontend separadas por vírgula.

### Endpoints

| Método | Rota | Uso |
| --- | --- | --- |
| POST | `/auth/register` | Criar registro em `tb_user` e emitir access token |
| POST | `/auth/login` | JSON com `email` e `password` |
| GET | `/auth/me` | Retornar ID, nome e email; exige Bearer |

Exemplo de cadastro:

```http
POST /auth/register
Content-Type: application/json
{"name":"Davi","email":"davi@gmail.com","password":"senha1234",
 "phone":null,"birthDate":"2000-01-01"}
```

O cadastro responde `201 Created` e retorna um access token. Emails são
normalizados para minúsculas e a senha precisa ter entre 8 e 256 caracteres.
Envie o token como `Authorization: Bearer <accessToken>` nas rotas protegidas.

Erros retornam 400 para entrada inválida, 401 para credenciais inválidas,
403 para acesso negado e 429 para excesso de tentativas.
O limite padrão é 30 tentativas por IP/minuto por instância, configurável por
`AUTH_ATTEMPTS_PER_MINUTE`. Múltiplas instâncias precisam de limite compartilhado
no gateway. As rotas de negócio exigem autenticação; as regras específicas de
permissão por recurso devem ser aplicadas pelos respectivos módulos.

### Testes

Execute `./mvnw test` (`mvnw.cmd test` no Windows). Os testes usam H2 e chaves
RSA temporárias, sem depender do PostgreSQL ou das chaves de produção.
