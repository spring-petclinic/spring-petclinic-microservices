data "aws_iam_policy_document" "node_assume" {
  statement {
    actions = ["sts:AssumeRole"]
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "node" {
  name               = "${var.cluster_name}-node"
  assume_role_policy = data.aws_iam_policy_document.node_assume.json
}

resource "aws_iam_role_policy_attachment" "node_worker" {
  role       = aws_iam_role.node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

resource "aws_iam_role_policy_attachment" "node_cni" {
  role       = aws_iam_role.node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

# Nodes pull images from ECR with this role. AWS requires the AmazonEC2ContainerRegistryReadOnly policy for the
# EKS add-on images (vpc-cni, kube-proxy, coredns live in an AWS-owned registry:
# 602401143452.dkr.ecr.ap-south-1.amazonaws.com). A policy limited to only this project's repositories
# blocks those images and the nodes never become Ready. It is read-only; tighten it in Phase 10 if desired
# (allow only this project's repositories plus the AWS add-on registry for your region).
resource "aws_iam_role_policy_attachment" "node_ecr_readonly" {
  role       = aws_iam_role.node.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_eks_node_group" "main" {
  cluster_name    = aws_eks_cluster.main.name
  node_group_name = "${var.cluster_name}-default"
  node_role_arn   = aws_iam_role.node.arn
  subnet_ids      = aws_subnet.public[*].id

  ami_type       = "AL2023_x86_64_STANDARD"
  capacity_type  = var.node_capacity_type
  instance_types = var.node_instance_types
  disk_size      = var.node_disk_gb

  scaling_config {
    min_size     = var.node_min
    desired_size = var.node_desired
    max_size     = var.node_max
  }

  update_config {
    max_unavailable = 1
  }

  labels = {
    workload = var.project
  }

  # Phase 8 (autoscaling) changes the desired size; do not let Terraform fight it
  lifecycle {
    ignore_changes = [scaling_config[0].desired_size]
  }

  depends_on = [
    aws_iam_role_policy_attachment.node_worker,
    aws_iam_role_policy_attachment.node_cni,
    aws_iam_role_policy_attachment.node_ecr_readonly,
  ]
}

# Managed add-ons. EKS installs vpc-cni, kube-proxy and CoreDNS itself at cluster creation;
# OVERWRITE lets Terraform adopt them. eks-pod-identity-agent is used by the Load Balancer Controller in Phase 6.
locals {
  addons = ["vpc-cni", "kube-proxy", "coredns", "eks-pod-identity-agent"]
}

resource "aws_eks_addon" "this" {
  for_each                    = toset(local.addons)
  cluster_name                = aws_eks_cluster.main.name
  addon_name                  = each.key
  resolve_conflicts_on_create = "OVERWRITE"
  resolve_conflicts_on_update = "OVERWRITE"

  depends_on = [aws_eks_node_group.main]
}
