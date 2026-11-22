data "aws_caller_identity" "current" {}

# Values published by the Phase 2 bootstrap stack
data "terraform_remote_state" "bootstrap" {
  backend = "s3"
  config = {
    bucket = var.state_bucket
    key    = "bootstrap/terraform.tfstate"
    region = var.aws_region
  }
}

# Jenkins uses an AWS-assigned public IP that changes on every stop/start, so look up the LIVE address
# at plan time (the value stored in the bootstrap state can be stale). The instance must be running.
data "aws_instance" "jenkins" {
  instance_id = data.terraform_remote_state.bootstrap.outputs.jenkins_instance_id
}

locals {
  jenkins_cidr = "${data.aws_instance.jenkins.public_ip}/32"
  api_cidrs    = distinct(concat([var.my_ip_cidr, local.jenkins_cidr], var.extra_api_cidrs))
}

data "aws_iam_policy_document" "eks_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["eks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "cluster" {
  name               = "${var.cluster_name}-cluster"
  assume_role_policy = data.aws_iam_policy_document.eks_assume.json
}

resource "aws_iam_role_policy_attachment" "cluster" {
  role       = aws_iam_role.cluster.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

resource "aws_eks_cluster" "main" {
  name                      = var.cluster_name
  version                   = var.kubernetes_version
  role_arn                  = aws_iam_role.cluster.arn
  enabled_cluster_log_types = var.enabled_cluster_log_types

  vpc_config {
    subnet_ids              = aws_subnet.public[*].id
    endpoint_private_access = true            # nodes talk to the API privately
    endpoint_public_access  = true            # kubectl from your laptop and Jenkins...
    public_access_cidrs     = local.api_cidrs # ...but ONLY from these addresses
  }

  # IAM access entries instead of the legacy aws-auth ConfigMap.
  # The identity that creates the cluster (you) is made cluster admin automatically.
  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = true
  }

  # Avoid surprise extended-support charges (several times the control-plane rate) after the
  # version leaves standard support: the cluster upgrades instead.
  upgrade_policy {
    support_type = "STANDARD"
  }

  lifecycle {
    precondition {
      condition     = data.aws_instance.jenkins.public_ip != ""
      error_message = "The Jenkins instance has no public IP. Start it first (bash scripts/jenkins-session.sh start), then run terraform again."
    }
  }

  depends_on = [aws_iam_role_policy_attachment.cluster]
}

# Jenkins: deploy rights in ONE namespace only (no cluster-wide access, no secrets/RBAC administration)
resource "aws_eks_access_entry" "jenkins" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = data.terraform_remote_state.bootstrap.outputs.jenkins_role_arn
  type          = "STANDARD"
}

resource "aws_eks_access_policy_association" "jenkins" {
  cluster_name  = aws_eks_cluster.main.name
  principal_arn = aws_eks_access_entry.jenkins.principal_arn
  policy_arn    = "arn:aws:eks::aws:cluster-access-policy/AmazonEKSEditPolicy"

  access_scope {
    type       = "namespace"
    namespaces = [var.app_namespace]
  }
}
