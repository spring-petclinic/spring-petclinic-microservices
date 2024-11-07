variable "resource_group_name" {}
variable "location" {}

resource "azurerm_sql_server" "sql_server" {
  name                         = "pets-sql-server"
  resource_group_name          = var.resource_group_name
  location                     = var.location
  version                      = "12.0"
  administrator_login          = "sqladmin"
  administrator_login_password = "P@ssw0rd123!"
}

resource "azurerm_sql_database" "sql_database" {
  name                = "pets-sql-database"
  resource_group_name = var.resource_group_name
  location            = var.location
  server_name         = azurerm_sql_server.sql_server.name
}

output "sql_database_id" {
  value = azurerm_sql_database.sql_database.id
}
