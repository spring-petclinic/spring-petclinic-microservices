output "aks_cluster_name" {
  description = "The name of the AKS cluster"
  value       = module.aks.cluster_name
}

output "aks_cluster_resource_group" {
  description = "The resource group of the AKS cluster"
  value       = module.aks.resource_group_name
}

output "aks_cluster_principal_id" {
  description = "The principal_id of the AKS cluster identity"
  value       = module.aks.principal_id
}