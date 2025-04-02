# Bật lại các deployment của petclinic
kubectl get deployment -n petclinic | Select-String -Pattern '^spring-petclinic' | ForEach-Object {
  $name = ($_ -split '\s+')[0]
  kubectl scale deployment $name --replicas=1 -n petclinic
}
Write-Host "All petclinic services are resumed (replicas=1)." -ForegroundColor Green
