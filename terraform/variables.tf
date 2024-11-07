# Nombre del grupo de recursos
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

variable "tenant_id" {
  description = "ID del tenant de Azure"
  type        = string
  default     = "26a7e7cf-9850-40df-89ab-a3170d67dcbf"
}