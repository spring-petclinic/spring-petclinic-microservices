helm upgrade --install spring-petclinic-customers-service-dev ./chart `
>>   --namespace dev `
>>   --create-namespace `
>>   --set image.repository="hzeroxium/spring-petclinic-customers-service" `
>>   --set image.tag="latest" `
>>   --set replicas=1 `
>>   --wait --timeout 180s
Release "spring-petclinic-customers-service-dev" has been upgraded. Happy Helming!
NAME: spring-petclinic-customers-service-dev
LAST DEPLOYED: Tue Mar 25 22:39:39 2025
NAMESPACE: dev
STATUS: deployed
REVISION: 3
TEST SUITE: None


docker build `
  -t hzeroxium/spring-petclinic-api-gateway:latest `
  --build-arg SERVICE_NAME=api-gateway `
  --build-arg EXPOSED_PORT=8080 `
  -f Dockerfile `
  .
