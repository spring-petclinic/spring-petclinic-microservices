#!/bin/bash
docker push ${REPOSITORY_PREFIX}/spring-petclinic-config-server:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-discovery-server:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-api-gateway:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-visits-service:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-vets-service:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-customers-service:${VERSION}
docker push ${REPOSITORY_PREFIX}/spring-petclinic-admin-server:${VERSION}
