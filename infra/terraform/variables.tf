variable "aws_region" {
  description = "AWS region for the backend infrastructure."
  type        = string
  default     = "us-east-1"
}

variable "artifact_bucket_name" {
  description = "Existing S3 bucket used by CodePipeline to store artifacts."
  type        = string
}

variable "codestar_connection_arn" {
  description = "Existing CodeStar/CodeConnections ARN connected to GitHub."
  type        = string
}

variable "github_repository_id" {
  description = "GitHub repository in owner/name format."
  type        = string
  default     = "evertonsilvagit/backend-extrato"
}

variable "github_branch" {
  description = "Git branch monitored by the deployment pipeline."
  type        = string
  default     = "main"
}

variable "vpc_id" {
  description = "VPC where the ALB and ECS tasks run."
  type        = string
}

variable "public_subnet_ids" {
  description = "Public subnet IDs used by the ALB and Fargate tasks."
  type        = list(string)
}

variable "certificate_arn" {
  description = "ACM certificate ARN used by the HTTPS listener."
  type        = string
}

variable "host_header" {
  description = "Public host header routed to the backend service."
  type        = string
}

variable "repository_name" {
  description = "ECR repository name."
  type        = string
  default     = "backend-extrato"
}

variable "cluster_name" {
  description = "ECS cluster name."
  type        = string
  default     = "default"
}

variable "service_name" {
  description = "Existing ECS service name used by deployments."
  type        = string
  default     = "backend-extrato"
}

variable "task_definition_family" {
  description = "ECS task definition family."
  type        = string
  default     = "default-backend-extrato"
}

variable "container_name" {
  description = "Container name inside the task definition."
  type        = string
  default     = "Main"
}

variable "container_port" {
  description = "Application container port."
  type        = number
  default     = 8083
}

variable "task_cpu" {
  description = "Fargate task CPU units."
  type        = number
  default     = 1024
}

variable "task_memory" {
  description = "Fargate task memory in MiB."
  type        = number
  default     = 2048
}

variable "manage_ecs_service" {
  description = "When true, Terraform will manage the ECS service. Keep false until import and review are complete."
  type        = bool
  default     = false
}

variable "service_desired_count" {
  description = "Desired task count for the ECS service."
  type        = number
  default     = 1
}

variable "health_check_grace_period_seconds" {
  description = "Grace period before ALB health checks count against new tasks."
  type        = number
  default     = 0
}

variable "log_group_name" {
  description = "CloudWatch log group used by the ECS task."
  type        = string
  default     = "/aws/ecs/default/backend-extrato-cf37"
}

variable "load_balancer_name" {
  description = "Application Load Balancer name."
  type        = string
  default     = "ecs-express-gateway-alb-cbc9a05e"
}

variable "alb_security_group_name" {
  description = "Security group name for the ALB."
  type        = string
  default     = "ecs-express-gateway-alb-sg-1774042761616"
}

variable "ecs_security_group_name" {
  description = "Security group name for the ECS service."
  type        = string
  default     = "default-backend-extrato-vpc-05c1fd7146a429cff"
}

variable "primary_target_group_name" {
  description = "Primary target group name used by the backend listener rule."
  type        = string
  default     = "ecs-gateway-tg-ea0397ac968132323"
}

variable "canary_target_group_name" {
  description = "Secondary target group name kept for canary/rollback traffic."
  type        = string
  default     = "ecs-gateway-tg-687d8b630cc55d1b1"
}

variable "pipeline_name" {
  description = "CodePipeline name."
  type        = string
  default     = "backend-extrato-pipeline"
}

variable "codebuild_project_name" {
  description = "CodeBuild project name."
  type        = string
  default     = "backend-extrato"
}

variable "execution_role_name" {
  description = "Existing ECS task execution role name."
  type        = string
  default     = "ecsTaskExecutionRole"
}

variable "codebuild_role_name" {
  description = "Existing IAM role name used by CodeBuild."
  type        = string
  default     = "codebuild-backend-extrato-service-role"
}

variable "codepipeline_role_name" {
  description = "Existing IAM role name used by CodePipeline."
  type        = string
  default     = "AWSCodePipelineServiceRole-us-east-1-backend-extrato-pipeline"
}

variable "container_environment" {
  description = "Plain-text environment variables injected into the backend container."
  type        = map(string)
  sensitive   = true
  default     = {}
}
