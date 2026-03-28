# API Backend Extrato

Documentacao pratica para consumo da API no Postman.

## Base URL

Use a aplicacao localmente em:

`http://localhost:8083`

Variavel sugerida no Postman:

- `baseUrl = http://localhost:8083`

## Ordem recomendada de uso

1. Criar uma `conta`
2. Criar `entradas`
3. Criar `dividas` se necessario
4. Criar lancamentos em `extratos` apontando para a `contaId`
5. Consultar o extrato filtrando por periodo

## Status codes

- `200 OK`: consulta realizada com sucesso
- `201 Created`: registro criado com sucesso
- `204 No Content`: registro removido com sucesso
- `400 Bad Request`: payload invalido
- `404 Not Found`: registro nao encontrado

## Contas

### POST `/api/contas`

Cria ou atualiza uma conta. Se `id` for enviado e existir, o registro e atualizado.

Body:

```json
{
  "id": null,
  "descricao": "Cartao Nubank",
  "valor": 1250.50,
  "diaPagamento": 10,
  "categoria": "CARTAO",
  "mesesVigencia": [1, 2, 3, 4, 5, 6]
}
```

Regras principais:

- `descricao` obrigatoria
- `valor` deve ser maior ou igual a `0`
- `diaPagamento` deve estar entre `1` e `31`

### GET `/api/contas`

Lista todas as contas.

### GET `/api/contas/{id}`

Busca uma conta por id.

### DELETE `/api/contas/{id}`

Remove uma conta por id.

## Entradas

### POST `/api/entradas`

Cria uma entrada.

Body:

```json
{
  "nome": "Salario",
  "tipo": "FIXA",
  "valor": 5000.00,
  "taxaImposto": 0,
  "mesesVigencia": [1, 2, 3, 4, 5, 6]
}
```

Regras principais:

- `nome` obrigatorio
- `tipo` obrigatorio
- `valor` deve ser maior que `0`
- `taxaImposto` deve ser maior ou igual a `0`
- `mesesVigencia` obrigatorio, com valores entre `1` e `12`

### GET `/api/entradas`

Lista entradas com paginacao simples.

Query params opcionais:

- `page`
- `size`

Exemplo:

`GET /api/entradas?page=0&size=20`

### GET `/api/entradas/{id}`

Busca uma entrada por id.

### DELETE `/api/entradas/{id}`

Remove uma entrada por id.

## Dividas

### POST `/api/dividas`

Cria ou atualiza uma divida. Se `id` for enviado e existir, o registro e atualizado.

Body:

```json
{
  "id": null,
  "description": "Financiamento moto",
  "amount": 890.75,
  "group": "TRANSPORTE"
}
```

Observacao:

- O JSON usa `description`, `amount` e `group`
- Internamente esses campos correspondem a `descricao`, `valor` e `grupo`

Regras principais:

- `description` obrigatorio
- `amount` deve ser maior ou igual a `0`
- `group` obrigatorio

### GET `/api/dividas`

Lista todas as dividas.

### DELETE `/api/dividas/{id}`

Remove uma divida por id.

## Extratos

### POST `/api/extratos`

Cria um lancamento ligado a uma conta.

Body:

```json
{
  "data": "2026-03-27",
  "tipo": "DEBITO",
  "valor": 150.90,
  "descricao": "Supermercado",
  "categoria": "ALIMENTACAO",
  "contaId": 1
}
```

Valores aceitos em `tipo`:

- `CREDITO`
- `DEBITO`

### GET `/api/extratos`

Consulta o extrato de uma conta.

Query params:

- `contaId` obrigatorio
- `de` opcional no formato `yyyy-MM-dd`
- `ate` opcional no formato `yyyy-MM-dd`
- `page` opcional
- `size` opcional

Exemplo:

`GET /api/extratos?contaId=1&de=2026-03-01&ate=2026-03-31&page=0&size=20`

Resposta exemplo:

```json
{
  "contaId": 1,
  "periodoDe": "2026-03-01",
  "periodoAte": "2026-03-31",
  "saldoAnterior": 0,
  "saldoAtual": 4849.10,
  "itens": [
    {
      "id": 10,
      "data": "2026-03-05",
      "tipo": "CREDITO",
      "valor": 5000.00,
      "descricao": "Salario",
      "categoria": "RENDA",
      "contaId": 1
    },
    {
      "id": 11,
      "data": "2026-03-27",
      "tipo": "DEBITO",
      "valor": 150.90,
      "descricao": "Supermercado",
      "categoria": "ALIMENTACAO",
      "contaId": 1
    }
  ]
}
```

### GET `/api/extratos/{id}`

Busca um lancamento por id.

### DELETE `/api/extratos/{id}`

Remove um lancamento por id.

## Actuator

### GET `/actuator/health`

Endpoint util para verificar se a aplicacao subiu corretamente.

## Notificacoes

### POST `/api/notificacoes/subscriptions`

Registra ou atualiza uma subscription Web Push por `endpoint`.

Body:

```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/abc123",
  "p256dh": "BOrandomBrowserKey",
  "auth": "randomAuthSecret",
  "userEmail": "user@example.com",
  "userName": "Everton"
}
```

Regras principais:

- `endpoint` obrigatorio
- `p256dh` obrigatorio
- `auth` obrigatorio
- se o `endpoint` ja existir, o cadastro e atualizado sem duplicar

### DELETE `/api/notificacoes/subscriptions`

Remove uma subscription pelo `endpoint`.

Body:

```json
{
  "endpoint": "https://fcm.googleapis.com/fcm/send/abc123"
}
```

Observacao:

- se a subscription nao existir, a API continua retornando sucesso (`204`)

### GET `/api/notificacoes/subscriptions`

Lista subscriptions salvas. Pode filtrar opcionalmente por `userEmail`.

Query params:

- `userEmail` opcional

Exemplo:

`GET /api/notificacoes/subscriptions?userEmail=user@example.com`

Resposta exemplo:

```json
[
  {
    "id": 1,
    "endpoint": "https://fcm.googleapis.com/fcm/send/abc123",
    "userEmail": "user@example.com",
    "userName": "Everton",
    "createdAt": "2026-03-28T14:00:00Z",
    "updatedAt": "2026-03-28T14:05:00Z"
  }
]
```

### GET `/api/notificacoes/status`

Retorna o estado atual da configuracao Web Push no backend.

Resposta exemplo:

```json
{
  "vapidConfigured": true,
  "publicKey": "BCEvMwXyNLgM8UwlajGSuE3x5NBn7jkCohTvxpMWwaafBwW4Z8qM-cSZPQaLiwL4Etl07haaYSVGDfRnrHTFFiU"
}
```

Uso recomendado:

- validar se o backend recebeu as envs VAPID
- comparar a `publicKey` retornada com `VITE_WEB_PUSH_PUBLIC_KEY` no frontend

### POST `/api/notificacoes/teste`

Dispara uma notificacao Web Push para todas as subscriptions ou filtra por `userEmail`.

Body:

```json
{
  "title": "Extrato atualizado",
  "body": "Um novo lancamento foi registrado.",
  "url": "/extrato",
  "userEmail": "user@example.com"
}
```

Resposta exemplo:

```json
{
  "targetCount": 1,
  "deliveredCount": 1,
  "removedCount": 0,
  "failedCount": 0
}
```

Configuracao obrigatoria no backend:

- `PUSH_VAPID_SUBJECT`
- `PUSH_VAPID_PUBLIC_KEY`
- `PUSH_VAPID_PRIVATE_KEY`

## Regenerando chaves VAPID

Quando o provedor retornar `403 Forbidden` no envio Web Push, o caso mais comum e:

- `PUSH_VAPID_PRIVATE_KEY` nao corresponde a `PUSH_VAPID_PUBLIC_KEY`
- ou a subscription atual do navegador foi criada com um par antigo

Fluxo recomendado para corrigir:

1. Gere um novo par VAPID
2. Atualize o backend com:
   - `PUSH_VAPID_SUBJECT`
   - `PUSH_VAPID_PUBLIC_KEY`
   - `PUSH_VAPID_PRIVATE_KEY`
3. Atualize o frontend com:
   - `VITE_WEB_PUSH_PUBLIC_KEY`
4. Reinicie backend e frontend
5. No navegador, desative e ative o push novamente para recriar a subscription

Comando sugerido para gerar as chaves:

```bash
npx web-push generate-vapid-keys
```

Exemplo de saida:

```text
=======================================

Public Key:
BCEvMwXyNLgM8UwlajGSuE3x5NBn7jkCohTvxpMWwaafBwW4Z8qM-cSZPQaLiwL4Etl07haaYSVGDfRnrHTFFiU

Private Key:
xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

=======================================
```

## Arquivo para importar no Postman

Collection pronta:

`docs/postman/backend-extrato.postman_collection.json`
