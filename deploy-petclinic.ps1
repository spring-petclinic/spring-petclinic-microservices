# deploy-petclinic.ps1
param (
  [string]$Namespace = "petclinic",
  [string]$Registry = "hzeroxium",
  [switch]$ExposeMirrored
)

kubectl create namespace $Namespace --dry-run=client -o yaml | kubectl apply -f -

$services = @(
  @{ Name = "config-server"; Port = 8888; Env = @(); DependsOn = @() },
  @{ Name = "discovery-server"; Port = 8761; Env = @(); DependsOn = @("config-server") },
  @{ Name = "customers-service"; Port = 8081; Env = @(); DependsOn = @("config-server", "discovery-server") },
  @{ Name = "visits-service"; Port = 8082; Env = @(); DependsOn = @("config-server", "discovery-server") },
  @{ Name = "vets-service"; Port = 8083; Env = @(); DependsOn = @("config-server", "discovery-server") },
  @{ Name = "genai-service"; Port = 8084; Env = @(); DependsOn = @("config-server", "discovery-server") 
  },
  @{ Name = "api-gateway"; Port = 8080; ExternalPort = 8085; Env = @(); DependsOn = @("config-server", "discovery-server") },
  @{ Name = "admin-server"; Port = 9090; Env = @(); DependsOn = @("config-server", "discovery-server") }
)

foreach ($service in $services) {
  Write-Host "Deploying $($service.Name)..." -ForegroundColor Cyan
  
  # Build env values string
  $envValues = ""
  if ($service.Env.Count -gt 0) {
    $envString = $service.Env | ForEach-Object { "{name: '$($_.Name)', value: '$($_.Value)'}" }
    $envValues = "--set env='{$($envString -join ',')}'"
  }
  
  $port = $service.Port
  
  # Build Helm command without using backticks inside the here-string
  $helmCommand = @"
helm upgrade --install spring-petclinic-$($service.Name) ./chart --namespace $Namespace --set image.repository="$Registry/spring-petclinic-$($service.Name)" --set image.tag="latest" --set service.port=$port --set replicas=1 $envValues --wait --timeout 180s
"@
  
  # Execute the command
  Invoke-Expression $helmCommand

  # Rest of the code remains the same
  if ($ExposeMirrored) {
    $externalPort = if ($service.ExternalPort) { $service.ExternalPort } else { $port }
    minikube service spring-petclinic-$($service.Name) -n $Namespace --url
    Write-Host "Service available at above URL (local port: $externalPort)" -ForegroundColor Green
  }

  Write-Host "Waiting for service to be ready..." -ForegroundColor Yellow
  Start-Sleep -Seconds 10
}

Write-Host "All services deployed successfully!" -ForegroundColor Green
