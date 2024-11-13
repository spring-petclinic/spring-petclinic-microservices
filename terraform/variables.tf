variable "subscription_id" {
  type        = string
  description = "ID de la suscripción de Azure"
  default     = "bf2233bb-8bc5-4ad0-abd7-340163356686"
}

variable "resource_group_name" {
  type        = string
  description = "Nombre del grupo de recursos"
  default     = "petDistri"
}

variable "location" {
  type        = string
  description = "Ubicación de los recursos"
  default     = "westus"
}

variable "aks_cluster_name" {
  type        = string
  description = "Nombre del clúster AKS"
  default     = "petDistriAKS"
}

variable "dns_prefix" {
  type        = string
  description = "Prefijo DNS para el clúster"
  default     = "clt"
}

variable "node_count" {
  type        = number
  description = "Número de nodos en el clúster"
  default     = 1
}

variable "vm_size" {
  type        = string
  description = "Tamaño de la máquina virtual de los nodos"
  default     = "standard_b4pls_v2"
}

variable "acr_name" {
  type        = string
  description = "Nombre del registro de contenedor de Azure"
  default     = "petDistriACR"
}

variable "mysql_server_name" {
  description = "Nombre del servidor MySQL"
  type        = string
  default     = "petclinicdbserver"
}

variable "mysql_admin_username" {
  description = "Usuario administrador de MySQL"
  type        = string
  default     = "adminuser"
}

variable "mysql_admin_password" {
  description = "Contraseña del administrador de MySQL"
  type        = string
  sensitive   = true
  default     = "TuContraseñaSegura"  # Cámbialo en producción
}

variable "mysql_db_name" {
  description = "Nombre de la base de datos MySQL"
  type        = string
  default     = "petclinic"
}

variable "key_vault_name" {
  description = "Nombre del Azure Key Vault"
  type        = string
  default     = "kvPetDistri"
}
