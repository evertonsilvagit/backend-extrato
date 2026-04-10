# Clean Architecture

O backend esta migrando de um modelo `controller/service/repository` para uma estrutura orientada por casos de uso.

## Estrutura

- `domain/<feature>`: entidades e regras centrais do negocio, sem dependencia de Spring.
- `application/<feature>/port/in`: contratos de entrada expostos para a camada externa.
- `application/<feature>/port/out`: portas que a aplicacao precisa para persistencia, mensageria ou integracoes.
- `application/<feature>/usecase`: implementacoes dos casos de uso.
- `infrastructure/<feature>`: adaptadores concretos para JPA, clients HTTP e outras tecnologias.
- `controller`: adaptadores HTTP. Controllers apenas traduzem request/response e chamam casos de uso.

## Feature piloto

A feature `CategoriaConta` ja segue esse desenho:

- `domain.categoriaconta.AccountCategory`
- `application.categoriaconta.*`
- `infrastructure.categoriaconta.AccountCategoryJpaAdapter`
- `controller.CategoriaContaController`

As features `CategoriaDivida`, `Conta`, `Divida`, `Entrada`, `Auth`, `Notificacoes` e `Assistant Chat` tambem foram migradas para o mesmo padrao.

## Regra de dependencia

As dependencias devem sempre apontar para dentro:

- `controller` depende de `application`
- `application` depende de `domain` e `port/out`
- `infrastructure` implementa `port/out`
- `domain` nao depende de Spring nem de JPA

## Proximo passo recomendado

Replicar o mesmo padrao nas proximas features, priorizando company profile, access control e fluxos restantes de integracao.
