provider "azurerm" {
  features {}
  subscription_id = var.subscription_id
}

# Grupo de recursos
resource "azurerm_resource_group" "example_rg" {
  name     = var.resource_group_name
  location = var.location
}

# Clúster AKS
resource "azurerm_kubernetes_cluster" "aks_cluster" {
  name                = var.aks_cluster_name
  location            = azurerm_resource_group.example_rg.location
  resource_group_name = azurerm_resource_group.example_rg.name
  dns_prefix          = var.dns_prefix

  default_node_pool {
    name       = "default"
    node_count = 1
    vm_size    = "standard_b2als_v2"
  }

  identity {
    type = "SystemAssigned"
  }

  network_profile {
    network_plugin    = "azure"
    load_balancer_sku = "standard"
  }

  tags = {
    environment = "Dev"
  }
}

# Registro de contenedor de Azure
resource "azurerm_container_registry" "acr" {
  name                = var.acr_name
  resource_group_name = azurerm_resource_group.example_rg.name
  location            = azurerm_resource_group.example_rg.location
  sku                 = "Basic"
  admin_enabled       = true
}

# Asignación de rol entre AKS y ACR
resource "azurerm_role_assignment" "aks_acr_role_assignment" {
  principal_id         = azurerm_kubernetes_cluster.aks_cluster.kubelet_identity[0].object_id
  role_definition_name = "AcrPull"
  scope                = azurerm_container_registry.acr.id
}
