# Helm Chart for this application 

This folder contains Helm repository for the built and run of this application.

## About helm

[Helm](https://helm.sh/docs/) can be seen as a package manager for kubernetes (like rpm, apt, apk ...)

### Tiller less helm

Helm use tiller in order to build ressource from template and to deploy ressources described in charts. Tiller need cluser wide privilideges. It can be seen has a security threat. Helm v3 is suppose to remove tiller in order to user user priviledge instead of tiller's. Meanwhile I suggest we use [Helm tiller plugin](https://github.com/rimusz/helm-tiller) aka Tillerless Helm


## Folder organisation

* [charts](charts) contains charts customized or retreive from "default" [helm repository](https://github.com/helm/charts)
* [manifests](manifests) contains flat yaml files use to the project, because somtime it's just simplier while we build the charts. I also use it has a depot using `helm template `


### Deployement Charts : CI

#### jenkins

@todo

#### sonar

@todo

#### nexus

@todo

#### dependency-track

In helm chart directory create the value file for your project.
For instance :

````bash
cp owasp/dependency-track/values.yaml ../manifest/values/helm-prod-values.yaml 
````

Edit the value you need
Notive you might need to create a StorageClass that allows gid 1000

Test your value 

````bash
helm template owasp/dependency-track  --output-dir=../manifest/ \
    -f ../manifest/values/helm-prod-values.yaml && \
   kubectl apply --dry-run \
   -f ../manifest/dependency-track/templates/`
````

Install the project in your namespace

````bash
helm install owasp/dependency-track --dry-run -f ../manifest/values/helm-prod-values.yaml
````

### Deployement Charts : Run

#### prometheus

@todo

#### grafana

@todo

#### spring-petclinic-admin-server

@todo

#### spring-petclinic-api-gateway

@todo

#### spring-petclinic-config-server

@todo

#### spring-petclinic-customers-service

@todo

#### spring-petclinic-discovery-server

@todo

#### spring-petclinic-hystrix-dashboard

@todo

#### spring-petclinic-vets-service

@todo

#### spring-petclinic-visits-service

## Remember

Annoted your ingress for https (networking.gke.io/managed-certificates: myservice.example.org)


## Reference and inspiration

I would like to thanks those blogs/githut/tweet for helping me 

* https://blog.giantswarm.io/what-you-yaml-is-what-you-get/
* https://twitter.com/learnk8s/status/1110549570990280709 


## 

