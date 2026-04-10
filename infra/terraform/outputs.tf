output "repository_url" {
  value       = aws_ecr_repository.backend.repository_url
  description = "ECR repository URL for the backend image."
}

output "alb_dns_name" {
  value       = aws_lb.backend.dns_name
  description = "Application Load Balancer DNS name."
}

output "listener_arn" {
  value       = aws_lb_listener.https.arn
  description = "HTTPS listener ARN."
}

output "ecs_cluster_name" {
  value       = aws_ecs_cluster.backend.name
  description = "ECS cluster managed by Terraform."
}

output "task_definition_arn" {
  value       = aws_ecs_task_definition.backend.arn
  description = "Latest task definition ARN created by Terraform."
}

output "ecs_service_name" {
  value       = var.manage_ecs_service ? aws_ecs_service.backend[0].name : var.service_name
  description = "ECS service name."
}

output "pipeline_name" {
  value       = aws_codepipeline.backend.name
  description = "Deployment pipeline name."
}
