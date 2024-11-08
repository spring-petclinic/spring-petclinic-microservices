output "mysql_server_fqdn" {
  description = "URL del servidor MySQL"
  value       = azurerm_mysql_flexible_server.mysql_server.fqdn
}

output "mysql_database_name" {
  description = "Nombre de la base de datos MySQL"
  value       = azurerm_mysql_flexible_database.petclinic_db.name
}

output "key_vault_id" {
  description = "ID del Key Vault"
  value       = azurerm_key_vault.key_vault.id
}

output "mysql_username_secret" {
  description = "Nombre del secreto del usuario de MySQL en Key Vault"
  value       = azurerm_key_vault_secret.db_username.name
}

output "mysql_password_secret" {
  description = "Nombre del secreto de la contraseña de MySQL en Key Vault"
  value       = azurerm_key_vault_secret.db_password.name
}
