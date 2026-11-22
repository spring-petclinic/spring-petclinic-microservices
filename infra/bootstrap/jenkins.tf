# Jenkins lives in the account's default VPC (free). The EKS VPC is created separately in Phase 4.
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "default-for-az"
    values = ["true"]
  }
}

# Canonical publishes the current Ubuntu 24.04 AMI id as a public SSM parameter
data "aws_ssm_parameter" "ubuntu_ami" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

resource "aws_security_group" "jenkins" {
  name        = "${var.project}-jenkins"
  description = "Jenkins controller. No SSH. Port 8080 only from explicitly listed CIDRs"
  vpc_id      = data.aws_vpc.default.id
}

resource "aws_vpc_security_group_ingress_rule" "jenkins_http" {
  for_each          = toset(var.jenkins_ingress_cidrs)
  security_group_id = aws_security_group.jenkins.id
  description       = "Jenkins 8080"
  cidr_ipv4         = each.value
  from_port         = 8080
  to_port           = 8080
  ip_protocol       = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "all" {
  security_group_id = aws_security_group.jenkins.id
  description       = "Outbound for apt, Maven Central, GitHub, ECR, EKS"
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_instance" "jenkins" {
  ami                         = data.aws_ssm_parameter.ubuntu_ami.value
  instance_type               = var.jenkins_instance_type
  subnet_id                   = sort(data.aws_subnets.default.ids)[0]
  vpc_security_group_ids      = [aws_security_group.jenkins.id]
  iam_instance_profile        = aws_iam_instance_profile.jenkins.name
  associate_public_ip_address = true # outbound internet; there is no inbound rule unless you add one

  # replace() strips any Windows carriage returns so the script still runs on Linux
  user_data = replace(templatefile("${path.module}/user_data.sh", {
    kubectl_minor = var.kubectl_minor
  }), "\r", "")

  metadata_options {
    http_tokens                 = "required" # IMDSv2 only
    http_endpoint               = "enabled"
    http_put_response_hop_limit = 1 # containers on the instance cannot read the instance-role credentials
  }

  root_block_device {
    volume_type = "gp3"
    volume_size = var.jenkins_volume_gb
    encrypted   = true
  }

  tags = {
    Name = "${var.project}-jenkins"
  }

  # Jenkins keeps its state on this disk. Do not let a newer AMI or edited bootstrap script
  # silently replace the instance (and wipe Jenkins). Replace deliberately: terraform apply -replace=aws_instance.jenkins
  lifecycle {
    ignore_changes = [ami, user_data, associate_public_ip_address]
  }
}

resource "aws_eip" "jenkins" {
  count    = var.use_elastic_ip ? 1 : 0
  domain   = "vpc"
  instance = aws_instance.jenkins.id

  tags = {
    Name = "${var.project}-jenkins"
  }
}
