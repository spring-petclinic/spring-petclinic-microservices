# Tắt tất cả pod bằng cách scale deployment về 0
kubectl scale deployment --all --replicas=0 -n petclinic
Write-Host "All petclinic services are now paused (replicas=0)." -ForegroundColor Yellow
