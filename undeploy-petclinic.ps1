# undeploy-petclinic.ps1
param (
  [string]$Namespace = "petclinic"
)

$services = @(
  "admin-server",
  "api-gateway",
  "genai-service",
  "vets-service",
  "visits-service",
  "customers-service",
  "discovery-server",
  "config-server"
)

foreach ($service in $services) {
  $release = "spring-petclinic-$service"
  Write-Host "Uninstalling $release..." -ForegroundColor Yellow
  helm uninstall $release -n $Namespace
}

Write-Host "All services have been uninstalled from namespace '$Namespace'." -ForegroundColor Green
