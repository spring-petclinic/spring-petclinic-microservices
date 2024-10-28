terraform {
  required_version = ">=1.0"

  required_providers {
    azapi = {
      source  = "azure/azapi"
      version = "~>1.5"
    }
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~>3.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~>3.0"
    }
    time = {
      source  = "hashicorp/time"
      version = "0.9.1"
    }
  }
}

provider "azurerm" {
  subscription_id = "26a7e7cf-9850-40df-89ab-a3170d67dcbf"
  features {}
}

# Crear el Resource Group
resource "azurerm_resource_group" "resource_group_pet_clinic" {
  name     = var.resource_group_name
  location = var.location
}


module "aks" {
  source              = "./modules/aks"
  resource_group_name = var.resource_group_name
  location            = var.location
}