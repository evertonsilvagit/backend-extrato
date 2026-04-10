resource "aws_security_group" "alb" {
  name        = var.alb_security_group_name
  description = "ECS Express Gateway security group - allows HTTP/HTTPS inbound with managed outbound rules"
  vpc_id      = var.vpc_id

  ingress {
    description      = "HTTP access from anywhere (IPv4)"
    from_port        = 80
    to_port          = 80
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  ingress {
    description      = "HTTPS access from anywhere (IPv4)"
    from_port        = 443
    to_port          = 443
    protocol         = "tcp"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  egress {
    description = "Placeholder rule to reserve capacity"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    self        = true
  }

  egress {
    description     = "Egress Rule to ECS Service"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs_service.id]
  }
}

resource "aws_security_group" "ecs_service" {
  name        = var.ecs_security_group_name
  description = "Security group for ECS service: ${var.service_name}"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Application traffic from ALB"
    from_port       = var.container_port
    to_port         = var.container_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  egress {
    description = "Allow all outbound traffic"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}

resource "aws_lb" "backend" {
  name               = var.load_balancer_name
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.alb.id]
  subnets            = var.public_subnet_ids
  ip_address_type    = "ipv4"
}

resource "aws_lb_target_group" "primary" {
  name             = var.primary_target_group_name
  port             = 80
  protocol         = "HTTP"
  protocol_version = "HTTP1"
  target_type      = "ip"
  vpc_id           = var.vpc_id

  health_check {
    enabled             = true
    healthy_threshold   = 5
    unhealthy_threshold = 2
    interval            = 30
    timeout             = 5
    path                = "/actuator/health"
    matcher             = "200"
    port                = var.container_port
    protocol            = "HTTP"
  }
}

resource "aws_lb_target_group" "canary" {
  name             = var.canary_target_group_name
  port             = 80
  protocol         = "HTTP"
  protocol_version = "HTTP1"
  target_type      = "ip"
  vpc_id           = var.vpc_id

  health_check {
    enabled             = true
    healthy_threshold   = 5
    unhealthy_threshold = 2
    interval            = 30
    timeout             = 5
    path                = "/actuator/health"
    matcher             = "200"
    port                = var.container_port
    protocol            = "HTTP"
  }
}

resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.backend.arn
  port              = 443
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = var.certificate_arn

  default_action {
    type = "fixed-response"

    fixed_response {
      content_type = "text/plain"
      message_body = "Not Found"
      status_code  = "404"
    }
  }
}

resource "aws_lb_listener_rule" "backend_host" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 1

  action {
    type = "forward"

    forward {
      target_group {
        arn    = aws_lb_target_group.primary.arn
        weight = 100
      }

      target_group {
        arn    = aws_lb_target_group.canary.arn
        weight = 0
      }
    }
  }

  condition {
    host_header {
      values = [var.host_header]
    }
  }
}

resource "aws_cloudwatch_metric_alarm" "rollback_alarm" {
  alarm_name          = "${var.cluster_name}/${var.service_name}/RollbackAlarm"
  alarm_description   = "Rate of 4XX and 5XX errors"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 3
  datapoints_to_alarm = 2
  threshold           = 1
  treat_missing_data  = "notBreaching"

  metric_query {
    id          = "m0_4xx"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "HTTPCode_Target_4XX_Count"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.canary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "m0_5xx"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "HTTPCode_Target_5XX_Count"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.canary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "m0_total"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "RequestCountPerTarget"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.canary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "em0"
    expression  = "100 * (IF(m0_4xx < 5, 0, m0_4xx) + m0_5xx) / FILL(m0_total, 1)"
    label       = "Sum of 4XX and 5XX errors percentage for canary target group"
    return_data = false
  }

  metric_query {
    id          = "m1_4xx"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "HTTPCode_Target_4XX_Count"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.primary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "m1_5xx"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "HTTPCode_Target_5XX_Count"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.primary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "m1_total"
    return_data = false

    metric {
      namespace   = "AWS/ApplicationELB"
      metric_name = "RequestCountPerTarget"
      period      = 60
      stat        = "Sum"
      dimensions = {
        LoadBalancer = aws_lb.backend.arn_suffix
        TargetGroup  = aws_lb_target_group.primary.arn_suffix
      }
    }
  }

  metric_query {
    id          = "em1"
    expression  = "100 * (IF(m1_4xx < 5, 0, m1_4xx) + m1_5xx) / FILL(m1_total, 1)"
    label       = "Sum of 4XX and 5XX errors percentage for primary target group"
    return_data = false
  }

  metric_query {
    id          = "e"
    expression  = "MAX([em0, em1])"
    label       = "Sum of 4XX and 5XX errors percentage"
    return_data = true
  }
}

