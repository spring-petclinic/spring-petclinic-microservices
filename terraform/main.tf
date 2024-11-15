provider "azurerm" {
  features {}
  subscription_id = var.subscription_id
}

terraform {
  backend "azurerm" {}
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
    node_count = 3
    vm_size    = "standard_b2als_v2"

    # Configuración explícita para evitar cambios constantes
    upgrade_settings {
      drain_timeout_in_minutes      = 0
      max_surge                     = "10%"
      node_soak_duration_in_minutes = 0
    }
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

# Servidor MySQL Flexible
resource "azurerm_mysql_flexible_server" "mysql_server" {
  name                = var.mysql_server_name
  location            = "westus"
  resource_group_name = azurerm_resource_group.example_rg.name

  # Configuración de la SKU
  sku_name = "B_Standard_B1ms"

  # Autenticación de administrador
  administrator_login    = var.mysql_admin_username
  administrator_password = var.mysql_admin_password

  # Configuración de almacenamiento
  storage {
    size_gb = 20  # Tamaño mínimo en GB
  }

  version = "5.7"
}

# Base de datos en el servidor MySQL
resource "azurerm_mysql_flexible_database" "petclinic_db" {
  name                = var.mysql_db_name
  resource_group_name = azurerm_resource_group.example_rg.name
  server_name         = azurerm_mysql_flexible_server.mysql_server.name
  charset             = "utf8"
  collation           = "utf8_general_ci"
}

# Datos del cliente necesarios para el Tenant ID
data "azurerm_client_config" "current" {}

# Azure Key Vault
resource "azurerm_key_vault" "key_vault" {
  name                        = var.key_vault_name
  location                    = "westus"
  resource_group_name         = azurerm_resource_group.example_rg.name
  tenant_id                   = data.azurerm_client_config.current.tenant_id
  sku_name                    = "standard"

  # Permitir que AKS acceda al Key Vault con permisos adicionales
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = azurerm_kubernetes_cluster.aks_cluster.identity[0].principal_id
    secret_permissions = ["Get", "List", "Set", "Delete", "Purge", "Recover", "Backup", "Restore"]
  }

  # Añadir acceso adicional para el objeto 1179b303-3b91-4deb-ba39-2265ebffcdc8
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = "1179b303-3b91-4deb-ba39-2265ebffcdc8"
    secret_permissions = ["Get", "List", "Set", "Delete", "Purge", "Recover", "Backup", "Restore"]
  }

  # Nueva política para el objeto 4410248c-0868-4814-ba94-d8f8bc53ae99
  access_policy {
    tenant_id = data.azurerm_client_config.current.tenant_id
    object_id = "4410248c-0868-4814-ba94-d8f8bc53ae99"
    secret_permissions = ["Get", "List"]
  }
}

# Cadena de conexión a MySQL como secreto en Key Vault
resource "azurerm_key_vault_secret" "db_connection_string" {
  name         = "DB-Connection-String"
  value        = "Server=${azurerm_mysql_flexible_server.mysql_server.fqdn};Database=${azurerm_mysql_flexible_database.petclinic_db.name};User Id=${var.mysql_admin_username};Password=${var.mysql_admin_password};"
  key_vault_id = azurerm_key_vault.key_vault.id
}

# Secretos adicionales para usuario y contraseña de MySQL en Key Vault
resource "azurerm_key_vault_secret" "db_username" {
  name         = "DB-Username"
  value        = var.mysql_admin_username
  key_vault_id = azurerm_key_vault.key_vault.id
}

resource "azurerm_key_vault_secret" "db_password" {
  name         = "DB-Password"
  value        = var.mysql_admin_password
  key_vault_id = azurerm_key_vault.key_vault.id
}
