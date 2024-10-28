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
