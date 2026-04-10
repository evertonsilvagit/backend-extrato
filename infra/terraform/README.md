# Terraform do backend-extrato

Esta pasta inicia a migração da infraestrutura do backend para Terraform com foco em:

- ECR
- CloudWatch Logs
- ALB, security groups, target groups e listener rule
- ECS cluster
- ECS task definition
- CodeBuild
- CodePipeline com deploy para ECS via `imagedefinitions.json`

## Remote state

Para uso em equipe, use remote state em S3 com lock em DynamoDB.

Os arquivos de bootstrap ficam em [bootstrap/README.md](C:/dev/projects/extrato/backend-extrato/infra/terraform/bootstrap/README.md).

Fluxo recomendado:

1. Entre em [bootstrap](C:/dev/projects/extrato/backend-extrato/infra/terraform/bootstrap/README.md) e crie o bucket/tabela do state.
2. Copie [backend.tf.example](C:/dev/projects/extrato/backend-extrato/infra/terraform/backend.tf.example) para `backend.tf`.
3. Preencha bucket e tabela criados.
4. Rode `terraform init -reconfigure` no diretório principal.

## Escopo desta primeira etapa

O serviço ECS atual foi criado pelo fluxo gerenciado do console e está usando comportamento avançado de deployment com dois target groups e rollback automático.

Para evitar uma troca arriscada de estratégia durante a migração:

- o `aws_ecs_service` agora existe como etapa opcional, desligada por padrão
- o pipeline já foi preparado para fazer deploy na service existente
- a task definition já foi modelada para que a próxima etapa seja gerenciar o serviço também

## Fase 2: gerenciar a ECS service

O arquivo [service.tf](C:/dev/projects/extrato/backend-extrato/infra/terraform/service.tf) adiciona a `aws_ecs_service`, mas ela só entra no plano quando `manage_ecs_service = true`.

Fluxo recomendado:

1. Faça o import da fase 1 com `manage_ecs_service = false`.
2. Garanta que o `terraform plan` está limpo ou com mudanças esperadas.
3. Mude `manage_ecs_service = true`.
4. Rode o import comentado em `import.ps1`.
5. Rode novo `terraform plan` e revise com atenção.

Observações:

- O recurso usa `prevent_destroy = true`.
- O recurso ignora `task_definition` e `desired_count` inicialmente, para não brigar com a pipeline/deploy atual.
- Se depois você quiser que Terraform assuma também a orquestração de deploy, a próxima etapa é remover esses `ignore_changes` e decidir se mantemos ou simplificamos o comportamento canary atual.

## Antes de usar

1. Copie `terraform.tfvars.example` para um arquivo local `terraform.tfvars`.
2. Preencha os segredos de `container_environment` fora do Git.
3. Se for usar remote state, faça antes o bootstrap descrito acima.
4. Rode `terraform init`.
5. Rode o script `import.ps1` para associar os recursos existentes ao estado.
6. Rode `terraform plan` e revise tudo antes de qualquer `apply`.

## Observações importantes

- O `buildspec.yml` do repositório foi ajustado para gerar `imagedefinitions.json` com o container `Main`, que é o nome real do container na task definition do ECS.
- O deploy stage do CodePipeline pressupõe que a service ECS `backend-extrato` continuará existindo.
- A estratégia de deployment avançada criada no console pode não ser representada 1:1 pelo provider atual. Por isso a `aws_ecs_service` entra numa fase separada e conservadora.
