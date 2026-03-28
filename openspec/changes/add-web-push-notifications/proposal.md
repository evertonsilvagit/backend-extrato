# Change: Add web push notifications backend support

## Why
O frontend já possui suporte para registrar subscriptions de Web Push, mas o backend ainda não consegue persistir essas subscriptions nem disparar notificações reais. Sem isso, o app fica limitado a testes locais no navegador.

## What Changes
- Adicionar persistência de subscriptions Web Push com endpoint público para cadastro e remoção
- Adicionar serviço para envio de notificações Web Push usando chaves VAPID
- Adicionar endpoint de teste para disparar uma notificação manualmente para subscriptions cadastradas
- Adicionar configuração por propriedades para chaves VAPID e assunto do emissor

## Impact
- Affected specs: `notifications`
- Affected code: `build.gradle`, `src/main/resources/application.properties`, novas classes em `controller`, `service`, `repository`, `model`, `dto`, novas migrations Flyway
