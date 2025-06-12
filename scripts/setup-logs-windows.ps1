# Create log directory for Spring Petclinic services
$logDir = "C:\logs\spring-petclinic"

# Create directory if it doesn't exist
if (-not (Test-Path -Path $logDir)) {
    New-Item -ItemType Directory -Path $logDir -Force | Out-Null
}

# Create log files for each service
$services = @("api-gateway", "customers-service", "visits-service", "vets-service")

foreach ($service in $services) {
    $logFile = Join-Path -Path $logDir -ChildPath "$service.log"
    if (-not (Test-Path -Path $logFile)) {
        New-Item -ItemType File -Path $logFile -Force | Out-Null
    }
}

Write-Host "Log directories and files have been created in $logDir"