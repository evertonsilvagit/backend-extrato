locals {
  repository_url = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/${var.repository_name}"

  buildspec_path = "${path.module}/../../buildspec.yml"

  container_environment = [
    for key in sort(keys(var.container_environment)) : {
      name  = key
      value = var.container_environment[key]
    }
  ]
}

