## Context
O app precisa enviar notificações Web Push para navegadores que registrarem uma `PushSubscription`. O frontend já envia `endpoint`, `p256dh`, `auth`, `userEmail` e `userName`, mas o backend não possui armazenamento nem serviço de envio.

## Goals / Non-Goals
- Goals:
  - Persistir subscriptions por navegador
  - Permitir cadastro e remoção idempotentes
  - Enviar notificações assinadas com VAPID
  - Expor um endpoint simples de teste para validação manual
- Non-Goals:
  - Implementar filas assíncronas
  - Implementar preferências avançadas por tipo de evento
  - Implementar autenticação real por usuário

## Decisions
- Decision: criar uma capability nova `notifications`
  - Alternatives considered: encaixar em `extratos` ou `entradas`
  - Rationale: Web Push é transversal e não pertence ao domínio financeiro principal
- Decision: persistir uma linha por `endpoint`
  - Alternatives considered: chave composta por usuário + endpoint
  - Rationale: o endpoint já identifica de forma prática a subscription do navegador
- Decision: criar endpoint de teste backend
  - Alternatives considered: depender apenas do teste local do frontend
  - Rationale: o fluxo completo precisa validar envio remoto real
- Decision: usar configuração via `application.properties`
  - Alternatives considered: valores hardcoded
  - Rationale: VAPID deve variar por ambiente

## Risks / Trade-offs
- Falha no envio para subscriptions expiradas
  - Mitigation: remover subscriptions inválidas quando o provedor retornar erro permanente
- Exposição de endpoint de teste
  - Mitigation: manter payload simples e documentado; futura autenticação pode restringir uso
- Dependência externa de biblioteca Web Push
  - Mitigation: encapsular o envio em um serviço dedicado

## Migration Plan
1. Criar tabela `push_subscription`
2. Expor endpoint de cadastro/remoção
3. Configurar chaves VAPID por ambiente
4. Validar com o frontend existente

## Open Questions
- O endpoint de teste deve enviar para todas as subscriptions ou aceitar filtro por email?
- A remoção deve ser silenciosa mesmo quando a subscription não existir?
