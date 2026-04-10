# MongoDB no backend-extrato

Este projeto continua usando PostgreSQL como banco principal para as entidades relacionais.
O MongoDB foi adicionado como uma persistencia de aprendizado em paralelo, sem substituir JPA/Flyway.

## Configuracao

Defina a variavel de ambiente abaixo antes de subir o backend:

```powershell
$env:SPRING_DATA_MONGODB_URI="mongodb://localhost:27017/extrato_learning"
```

Ou rode o script:

```powershell
.\run-backend.ps1 -MongoUri "mongodb://localhost:27017/extrato_learning"
```

## Endpoint de estudo

Base path:

```text
/api/mongo-learning/events
```

Criar evento:

```json
{
  "title": "Importacao de fatura",
  "type": "invoice-import",
  "payload": {
    "source": "whatsapp",
    "totalItems": 18,
    "bank": "nubank"
  }
}
```

Campos pensados para mostrar quando Mongo faz sentido:

- `title`: resumo humano do evento
- `type`: categoria do documento
- `payload`: estrutura livre e flexivel
- `createdAt`: data de criacao

## Quando usar Mongo aqui

Boas candidatas:

- logs de auditoria
- historico de importacoes
- eventos de integracao
- dados semi-estruturados

Nao e um bom primeiro alvo neste projeto:

- `Conta`
- `Divida`
- `Entrada`
- entidades com forte relacionamento entre si

## Endpoints reais no app

### Auditoria

Base path:

```text
/api/mongo-audit/events
```

Criar evento manual (opcional):

```json
{
  "action": "invoice-import",
  "resource": "invoice-records",
  "source": "api",
  "payload": {
    "created": 2,
    "updated": 1,
    "total": 3
  }
}
```

### Historico de importacoes

Base path:

```text
/api/mongo-imports/invoices
```

Esse log e alimentado automaticamente ao importar notas fiscais.

### Logs de notificacoes

Base path:

```text
/api/mongo-notifications/logs
```

Esse log e alimentado automaticamente ao enviar notificacoes (teste ou reais).
