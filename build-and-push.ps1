# build-and-push.ps1
param (
  [Parameter(Mandatory = $false)]
  [string]$Service = "all",
    
  [Parameter(Mandatory = $false)]
  [string]$Tag = "latest",
    
  [Parameter(Mandatory = $false)]
  [string]$Registry = "hzeroxium",
    
  [Parameter(Mandatory = $false)]
  [switch]$PushImages = $false,
    
  [Parameter(Mandatory = $false)]
  [switch]$SkipMavenBuild = $false
)

# Define service configurations
$services = @{
  "config-server"     = @{
    Port      = 8888
    SkipBuild = $false
  }
  "discovery-server"  = @{
    Port      = 8761
    SkipBuild = $false
  }
  "customers-service" = @{
    Port      = 8081
    SkipBuild = $false
  }
  "visits-service"    = @{
    Port      = 8082
    SkipBuild = $false
  }
  "vets-service"      = @{
    Port      = 8083
    SkipBuild = $false
  }
  "genai-service"     = @{
    Port      = 8084
    SkipBuild = $false
  }
  "api-gateway"       = @{
    Port      = 8080
    SkipBuild = $false
  }
  "admin-server"      = @{
    Port      = 9090
    SkipBuild = $false
  }
}

# Function to build a service
function Build-Service {
  param (
    [string]$ServiceName,
    [int]$Port,
    [string]$Tag
  )
    
  Write-Host "🔨 Building service: $ServiceName (Port: $Port)" -ForegroundColor Cyan
    
  # Run Maven build if not skipped
  if (-not $SkipMavenBuild) {
    Write-Host "  ⏳ Running Maven build..." -ForegroundColor Yellow
    mvn clean package -DskipTests -pl spring-petclinic-$ServiceName
        
    # Check if Maven build was successful
    if ($LASTEXITCODE -ne 0) {
      Write-Host "  ❌ Maven build failed for $ServiceName" -ForegroundColor Red
      return $false
    }
  }
    
  # Build Docker image
  Write-Host "  🐳 Building Docker image..." -ForegroundColor Yellow
  docker build `
    -t $Registry/spring-petclinic-$ServiceName`:$Tag `
    --build-arg SERVICE_NAME=$ServiceName `
    --build-arg EXPOSED_PORT=$Port `
    -f Dockerfile `
    .
        
  # Check if Docker build was successful
  if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Docker build failed for $ServiceName" -ForegroundColor Red
    return $false
  }
    
  # Push image if requested
  if ($PushImages) {
    Write-Host "  ⬆️ Pushing Docker image..." -ForegroundColor Yellow
    docker push $Registry/spring-petclinic-$ServiceName`:$Tag
        
    if ($LASTEXITCODE -ne 0) {
      Write-Host "  ❌ Docker push failed for $ServiceName" -ForegroundColor Red
      return $false
    }
  }
    
  Write-Host "  ✅ Successfully processed $ServiceName" -ForegroundColor Green
  return $true
}

# Check if we need to build all services or just one
if ($Service -eq "all") {
  # Build all services
  Write-Host "🚀 Building all Spring PetClinic services with tag: $Tag" -ForegroundColor Magenta
    
  $successCount = 0
  $failCount = 0
    
  foreach ($svc in $services.Keys) {
    $config = $services[$svc]
    $result = Build-Service -ServiceName $svc -Port $config.Port -Tag $Tag
        
    if ($result) { $successCount++ } else { $failCount++ }
    Write-Host "" # Empty line for better readability
  }
    
  # Summary
  Write-Host "📋 Build Summary:" -ForegroundColor Magenta
  Write-Host "  ✅ Successful: $successCount" -ForegroundColor Green
  Write-Host "  ❌ Failed: $failCount" -ForegroundColor Red
    
}
else {
  # Build just the specified service
  if ($services.ContainsKey($Service)) {
    $config = $services[$Service]
    $result = Build-Service -ServiceName $Service -Port $config.Port -Tag $Tag
        
    if ($result) {
      Write-Host "✅ Service $Service successfully built and processed" -ForegroundColor Green
    }
    else {
      Write-Host "❌ Service $Service build or processing failed" -ForegroundColor Red
    }
  }
  else {
    Write-Host "❌ Unknown service: $Service" -ForegroundColor Red
    Write-Host "Available services: $($services.Keys -join ', ')" -ForegroundColor Yellow
  }
}


kubectl expose service spring-petclinic-config-server `
--name=config-server `
--port=8888 `
--target-port=8888 `
--namespace=petclinic
