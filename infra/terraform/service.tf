resource "aws_ecs_service" "backend" {
  count = var.manage_ecs_service ? 1 : 0

  name            = var.service_name
  cluster         = aws_ecs_cluster.backend.id
  task_definition = aws_ecs_task_definition.backend.arn
  desired_count   = var.service_desired_count
  launch_type     = "FARGATE"

  scheduling_strategy                = "REPLICA"
  health_check_grace_period_seconds  = var.health_check_grace_period_seconds
  enable_ecs_managed_tags            = true
  propagate_tags                     = "SERVICE"
  availability_zone_rebalancing      = "ENABLED"
  wait_for_steady_state              = false

  deployment_controller {
    type = "ECS"
  }

  deployment_circuit_breaker {
    enable   = true
    rollback = true
  }

  deployment_maximum_percent         = 200
  deployment_minimum_healthy_percent = 100

  network_configuration {
    subnets          = var.public_subnet_ids
    security_groups  = [aws_security_group.ecs_service.id]
    assign_public_ip = true
  }

  lifecycle {
    prevent_destroy = true

    # The live service was originally created by the ECS console with advanced
    # managed deployment behavior. We ignore the task definition here so the
    # existing deployment pipeline can continue updating it independently until
    # we intentionally move deployment orchestration fully into Terraform.
    ignore_changes = [
      desired_count,
      task_definition,
    ]
  }

  depends_on = [
    aws_lb_listener_rule.backend_host,
    aws_cloudwatch_metric_alarm.rollback_alarm,
  ]
}
