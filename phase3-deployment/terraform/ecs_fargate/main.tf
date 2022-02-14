provider "aws" {
  region = "us-east-1"
}

resource "aws_cloudwatch_log_group" "ecs_fargate" {
  name              = "ecs_fargate"
  retention_in_days = 1
  tags              = {
    Project = "twitter-phase-3"
  }
}

resource "aws_ecs_task_definition" "hello_world" {
  family = "hello_world"

  container_definitions = <<EOF
[
  {
    "name": "hello_world",
    "image": "hello-world",
    "cpu": 0,
    "memory": 128,
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-region": "eu-west-1",
        "awslogs-group": "hello_world",
        "awslogs-stream-prefix": "complete-ecs"
      }
    }
  }
]
EOF
}

resource "aws_ecs_cluster" "ecs_cluster" {
  name = "ecs_cluster"
}