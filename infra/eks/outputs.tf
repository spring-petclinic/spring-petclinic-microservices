output "cluster_name" {
  value = aws_eks_cluster.main.name
}

output "cluster_endpoint" {
  value = aws_eks_cluster.main.endpoint
}

output "kubeconfig_command" {
  description = "Run on your laptop (add --profile <your-profile>) and on Jenkins (as the jenkins user)"
  value       = "aws eks update-kubeconfig --name ${aws_eks_cluster.main.name} --region ${var.aws_region}"
}

output "vpc_id" {
  value = aws_vpc.main.id
}

output "public_subnet_ids" {
  value = aws_subnet.public[*].id
}

output "node_role_arn" {
  value = aws_iam_role.node.arn
}

output "api_access_cidrs" {
  description = "Addresses allowed to reach the Kubernetes API"
  value       = local.api_cidrs
}
