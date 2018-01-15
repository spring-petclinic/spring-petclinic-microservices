SHELL := /bin/bash

all: build

travis_setup:
	scripts/install_dependencies.sh

local_registry: kubecmd := $(shell kubectl apply -f ./registry.yaml)
local_registry: fragment := $(shell minikube docker-env | head -n -2 | sed ':a;N;$$!ba; s/\n/ /g; s/export / /g')
local_registry: export DOCKER_REGISTRY=localhost:5000
local_registry:
	${fragment} mvn -X clean install -PdeployToRegistry

travis_registry:
	mvn clean install -PdeployToRegistry

local_deploy: local_registry
	KUBERNETES_IP=`minikube ip` mvn -X ansible:playbook -PdeployToK8s -Dansible.playbook=deploy-kube.yaml

travis_deploy: travis_registry
	mvn -X ansible:playbook -PdeployToK8s -Dansible.playbook=deploy-kube.yaml

local_clean:
	mvn clean
	rm -rf templates/output/*
