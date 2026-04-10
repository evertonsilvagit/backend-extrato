# Bootstrap do remote state

Esta pasta cria os recursos mínimos para o remote state do Terraform:

- bucket S3 para `tfstate`
- tabela DynamoDB para lock

## Uso

1. Copie `terraform.tfvars.example` para `terraform.tfvars`.
2. Escolha nomes globais e únicos para o bucket S3.
3. Rode `terraform init`.
4. Rode `terraform apply`.

## Depois do bootstrap

1. Copie `../backend.tf.example` para `../backend.tf`.
2. Preencha o bucket e a tabela criados.
3. No diretório principal do Terraform, rode `terraform init -reconfigure`.

## Observação

Faça o bootstrap uma única vez por ambiente compartilhado. Depois disso, o restante da infraestrutura deve usar sempre o backend remoto.

