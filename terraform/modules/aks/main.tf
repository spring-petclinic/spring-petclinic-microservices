resource "azurerm_kubernetes_cluster" "aks_pet" {
  name                = "aks_pet"
  location            = var.location
  resource_group_name = var.resource_group_name
  dns_prefix          = "akspetdns"

  default_node_pool {
    name       = "petnodepool"
    node_count = 1
    vm_size    = "standard_b2als_v2"
  }

  identity {
    type = "SystemAssigned"
  }

  tags = {
    Environment = "Production"
  }
}

