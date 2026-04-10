data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

data "aws_region" "current" {}

data "aws_iam_role" "ecs_execution" {
  name = var.execution_role_name
}

data "aws_iam_role" "codebuild" {
  name = var.codebuild_role_name
}

data "aws_iam_role" "codepipeline" {
  name = var.codepipeline_role_name
}

