SHELL := /bin/bash


all: build

travis_setup:
	scripts/install_dependencies.sh

local_registry: export DOCKER_API_VERSION=1.23
local_registry: export DOCKER_TLS_VERIFY=1
local_registry: export DOCKER_HOST=tcp://192.168.39.43:2376
local_registry: export DOCKER_CERT_PATH=~/.minikube/certs
local_registry: export DOCKER_REGISTRY=localhost:5000
local_registry:
	mvn clean install -PdeployToRegistry

travis_registry:
	mvn clean install -PdeployToRegistry

local_deploy: local_registry
	mvn -X ansible:playbook -PdeployToK8s -Dansible.playbook=deploy-kube.yaml

travis_deploy: travis_registry
	mvn -X ansible:playbook -PdeployToK8s -Dansible.playbook=deploy-kube.yaml

local_clean:
	mvn clean
	rm -rf templates/output/*
