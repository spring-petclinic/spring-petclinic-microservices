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
