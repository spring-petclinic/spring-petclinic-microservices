variable "resource_group_name" {
  description = "Nombre del grupo de recursos"
  type        = string
  default     = "rg-aks-pet-clinic"
}

variable "location" {
  description = "Ubicación donde se desplegarán los recursos"
  type        = string
  default     = "westus"
}

resource "azurerm_kubernetes_cluster" "aks_pet" {
  name                = "aks_pet"
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = "akspetdns"

  default_node_pool {
    name       = "petnodepool"
    node_count = 2
    vm_size    = "standard_b2als_v2"
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "Production"
  }
}


output "principal_id" {
  value = azurerm_kubernetes_cluster.aks_pet.identity[0].principal_id
  description = "El principal_id de la identidad asignada del clúster de AKS"
}

output "cluster_name" {
  value = azurerm_kubernetes_cluster.aks_pet.name
  description = "El nombre del clúster de AKS"
}

output "resource_group_name" {
  value = azurerm_kubernetes_cluster.aks_pet.node_resource_group
  description = "El grupo de recursos del clúster de AKS"
}

