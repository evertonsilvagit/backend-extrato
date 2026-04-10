terraform import aws_ecr_repository.backend backend-extrato
terraform import aws_cloudwatch_log_group.ecs "/aws/ecs/default/backend-extrato-cf37"
terraform import aws_security_group.alb sg-028b2b101d4bca10a
terraform import aws_security_group.ecs_service sg-01a5ce28e78e91a1d
terraform import aws_lb.backend arn:aws:elasticloadbalancing:us-east-1:116671358684:loadbalancer/app/ecs-express-gateway-alb-cbc9a05e/1b712109c5958457
terraform import aws_lb_target_group.primary arn:aws:elasticloadbalancing:us-east-1:116671358684:targetgroup/ecs-gateway-tg-ea0397ac968132323/3001fcdc25f20d4e
terraform import aws_lb_target_group.canary arn:aws:elasticloadbalancing:us-east-1:116671358684:targetgroup/ecs-gateway-tg-687d8b630cc55d1b1/7a3014c435a9514a
terraform import aws_lb_listener.https arn:aws:elasticloadbalancing:us-east-1:116671358684:listener/app/ecs-express-gateway-alb-cbc9a05e/1b712109c5958457/127829ef45a86801
terraform import aws_lb_listener_rule.backend_host arn:aws:elasticloadbalancing:us-east-1:116671358684:listener-rule/app/ecs-express-gateway-alb-cbc9a05e/1b712109c5958457/127829ef45a86801/ae79990fbca063b7
terraform import aws_cloudwatch_metric_alarm.rollback_alarm default/backend-extrato/RollbackAlarm
terraform import aws_ecs_cluster.backend default
terraform import aws_codebuild_project.backend backend-extrato
terraform import aws_codepipeline.backend backend-extrato-pipeline

# Run only after setting manage_ecs_service = true in terraform.tfvars.
# terraform import aws_ecs_service.backend[0] default/backend-extrato
