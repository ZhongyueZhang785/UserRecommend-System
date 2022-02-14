provider "aws" {
  region = "us-east-1"
}

resource "random_password" "master" {
  length = 10
}


module "aurora" {
  source  = "terraform-aws-modules/rds-aurora/aws"
  name           = "aurora-twitter-db"
  engine         = "aurora-mysql"
  engine_version = "5.10.1"
  instances = {
    1 = {
      instance_class      = "db.r6g.large"
      publicly_accessible = true
    }
  }

  vpc_id                 = "vpc-0f1af29b0eccc3462"
  db_subnet_group_name   = "default-vpc-0f1af29b0eccc3462"
  subnets = ["subnet-0046d0368e8f6bee9"]
  create_security_group  = true

  iam_database_authentication_enabled = true
  master_password                     = random_password.master.result
  create_random_password              = false

  apply_immediately   = true
  skip_final_snapshot = true

  db_parameter_group_name         = "default"
  db_cluster_parameter_group_name = "default"
  enabled_cloudwatch_logs_exports = ["audit", "error", "general", "slowquery"]

  tags = {
    Project = "twitter-phase-3"
  }
}