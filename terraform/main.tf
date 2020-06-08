provider "azurerm" {
  features {}
}

variable "resource_group" {
    type = string
}
variable "region" {
    type = string
}
variable "spring_cloud_service" {
    type = string
}
variable "api_gateway" {
    type = string
}
variable "admin_server" {
    type = string
}
variable "customers_service" {
    type = string
}
variable "visits_service" {
    type = string
}
variable "vets_service" {
    type = string
}
variable "mysql_server_name" {
    type = string
}
variable "mysql_server_admin_name" {
    type = string
}
variable "mysql_server_admin_password" {
    type = string
}
variable "mysql_database_name" {
    type = string
}
variable "dev_machine_ip" {
    type = string
}

resource "azurerm_resource_group" "example" {
  name     = var.resource_group
  location =  var.region
}

resource "azurerm_spring_cloud_service" "example" {
  name                = var.spring_cloud_service
  resource_group_name = azurerm_resource_group.example.name
  location            = azurerm_resource_group.example.location

  config_server_git_setting {
    uri          = "https://github.com/selvasingh/spring-petclinic-microservices-config"
    label        = "master"
    search_paths= ["."]

    }

  tags = {
    Env = "staging"
  }
}

resource "azurerm_spring_cloud_app" "api_gateway" {
  name                = var.api_gateway
  resource_group_name = azurerm_resource_group.example.name
  service_name        = azurerm_spring_cloud_service.example.name
}


resource "azurerm_spring_cloud_app" "admin_server" {
  name                = var.admin_server
  resource_group_name = azurerm_resource_group.example.name
  service_name        = azurerm_spring_cloud_service.example.name
}

resource "azurerm_spring_cloud_app" "customers_service" {
  name                = var.customers_service
  resource_group_name = azurerm_resource_group.example.name
  service_name        = azurerm_spring_cloud_service.example.name
}

resource "azurerm_spring_cloud_app" "vets_service" {
  name                = var.vets_service
  resource_group_name = azurerm_resource_group.example.name
  service_name        = azurerm_spring_cloud_service.example.name
}

resource "azurerm_spring_cloud_app" "visits_service" {
  name                = var.visits_service
  resource_group_name = azurerm_resource_group.example.name
  service_name        = azurerm_spring_cloud_service.example.name
}


resource "azurerm_mysql_server" "example" {
  name                = var.mysql_server_name
  location            = azurerm_resource_group.example.location
  resource_group_name = azurerm_resource_group.example.name

  sku_name = "GP_Gen5_2"

  storage_mb = 5120
  backup_retention_days = 7
  geo_redundant_backup_enabled = true

  administrator_login          = var.mysql_server_admin_name
  administrator_login_password = var.mysql_server_admin_password
  version                      = "5.7"
  ssl_enforcement_enabled =true
}

resource "azurerm_mysql_database" "example" {
  name                = var.mysql_database_name
  resource_group_name = azurerm_resource_group.example.name
  server_name         = azurerm_mysql_server.example.name
  charset             = "utf8"
  collation           = "utf8_unicode_ci"
}

resource "azurerm_mysql_firewall_rule" "allazureips" {
  name                = "allAzureIPs"
  resource_group_name = azurerm_resource_group.example.name
  server_name         = azurerm_mysql_server.example.name
  start_ip_address    = "0.0.0.0"
  end_ip_address      = "0.0.0.0"
}


resource "azurerm_mysql_firewall_rule" "devMachine" {
  name                = "devMachine"
  resource_group_name = azurerm_resource_group.example.name
  server_name         = azurerm_mysql_server.example.name
  start_ip_address    = var.dev_machine_ip
  end_ip_address      = var.dev_machine_ip
}

resource "azurerm_mysql_configuration" "example" {
  name                = "interactive_timeout"
  resource_group_name = azurerm_resource_group.example.name
  server_name         = azurerm_mysql_server.example.name
  value               = "2147483"
}

resource "azurerm_mysql_configuration" "time_zone" {
  name                = "time_zone"
  resource_group_name = azurerm_resource_group.example.name
  server_name         = azurerm_mysql_server.example.name
  value               = "-8:00" // Add appropriate offset based on your region.
}
