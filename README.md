---
page_type: sample
languages:
- java
products:
- Azure Spring Cloud
description: "Deploy Spring microservices using Azure Spring Cloud and MySQL"
urlFragment: "spring-petclinic-microservices"
---

![Deploy to Azure Spring Cloud with Key Vault](https://github.com/Azure-Samples/spring-petclinic-microservices/workflows/Deploy%20to%20Azure%20Spring%20Cloud%20with%20Key%20Vault/badge.svg)

# Deploy Spring Microservices using Azure Spring Cloud and MySQL 

Azure Spring Cloud enables you to easily run a Spring Boot based microservices application on Azure.

This quickstart shows you how to deploy an existing Java Spring Cloud application to Azure. When you're finished, you can continue to manage the application via the Azure CLI or switch to using the Azure portal.

## What will you experience
You will:
- Build existing Spring microservices applications
- Provision an Azure Spring Cloud service instance. If you prefer Terraform, you may also provision using Terraform, see [`README-terraform`](./terraform/README-terraform.md)
- Deploy applications to Azure
- Bind applications to Azure Database for MySQL
- Open the application

## What you will need

In order to deploy a Java app to cloud, you need 
an Azure subscription. If you do not already have an Azure 
subscription, you can activate your 
[MSDN subscriber benefits](https://azure.microsoft.com/pricing/member-offers/msdn-benefits-details/) 
or sign up for a 
[free Azure account]((https://azure.microsoft.com/free/)).

In addition, you will need the following:

| [Azure CLI version 2.0.67 or higher](https://docs.microsoft.com/cli/azure/install-azure-cli?view=azure-cli-latest) 
| [Java 8](https://www.azul.com/downloads/azure-only/zulu/?version=java-8-lts&architecture=x86-64-bit&package=jdk) 
| [Maven](https://maven.apache.org/download.cgi) 
| [MySQL CLI](https://dev.mysql.com/downloads/shell/)
| [Git](https://git-scm.com/)
|

## Install the Azure CLI extension

Install the Azure Spring Cloud extension for the Azure CLI using the following command

```bash
    az extension add --name spring-cloud
```

## Clone and build the repo

### Create a new folder and clone the sample app repository to your Azure Cloud account  

```bash
    mkdir source-code
    git clone https://github.com/azure-samples/spring-petclinic-microservices
```

### Change directory and build the project

```bash
    cd spring-petclinic-microservices
    mvn clean package -DskipTests -Denv=cloud
```
This will take a few minutes.

## Provision Azure Spring Cloud service instance using Azure CLI

### Prepare your environment for deployments

Create a bash script with environment variables by making a copy of the supplied template:
```bash
    cp .scripts/setup-env-variables-azure-template.sh .scripts/setup-env-variables-azure.sh
```

Open `.scripts/setup-env-variables-azure.sh` and enter the following information:

```bash

    export SUBSCRIPTION=subscription-id # customize this
    export RESOURCE_GROUP=resource-group-name # customize this
    ...
    export SPRING_CLOUD_SERVICE=azure-spring-cloud-name # customize this
    ...
    export MYSQL_SERVER_NAME=mysql-servername # customize this
    ...
    export MYSQL_SERVER_ADMIN_NAME=admin-name # customize this
    ...
    export MYSQL_SERVER_ADMIN_PASSWORD=SuperS3cr3t # customize this
    ...
```

Then, set the environment:
```bash
    source .scripts/setup-env-variables-azure.sh
```

### Login to Azure 
Login to the Azure CLI and choose your active subscription. Be sure to choose the active subscription that is whitelisted for Azure Spring Cloud

```bash
    az login
    az account list -o table
    az account set --subscription ${SUBSCRIPTION}
```

### Create Azure Spring Cloud service instance
Prepare a name for your Azure Spring Cloud service.  The name must be between 4 and 32 characters long and can contain only lowercase letters, numbers, and hyphens.  The first character of the service name must be a letter and the last character must be either a letter or a number.

Create a resource group to contain your Azure Spring Cloud service.

```bash
    az group create --name ${RESOURCE_GROUP} \
        --location ${REGION}
```

Create an instance of Azure Spring Cloud.

```bash
    az spring-cloud create --name ${SPRING_CLOUD_SERVICE} \
        --resource-group ${RESOURCE_GROUP} \
        --location ${REGION}
```

The service instance will take around five minutes to deploy.

Set your default resource group name and cluster name using the following commands:

```bash
    az configure --defaults \
        group=${RESOURCE_GROUP} \
        location=${REGION} \
        spring-cloud=${SPRING_CLOUD_SERVICE}
```

### Load Spring Cloud Config Server

Use the `application.yml` in the root of this project to load configuration into the Config Server in Azure Spring Cloud.

```bash
    az spring-cloud config-server set \
        --config-file application.yml \
        --name ${SPRING_CLOUD_SERVICE}
```

## Create microservice applications

Create 5 microservice apps.

```bash
    az spring-cloud app create --name ${API_GATEWAY} --instance-count 1 --is-public true \
        --memory 2 \
        --jvm-options='-Xms2048m -Xmx2048m'
    
    az spring-cloud app create --name ${ADMIN_SERVER} --instance-count 1 --is-public true \
        --memory 2 \
        --jvm-options='-Xms2048m -Xmx2048m'
    
    az spring-cloud app create --name ${CUSTOMERS_SERVICE} --instance-count 1 \
        --memory 2 \
        --jvm-options='-Xms2048m -Xmx2048m'
    
    az spring-cloud app create --name ${VETS_SERVICE} --instance-count 1 \
        --memory 2 \
        --jvm-options='-Xms2048m -Xmx2048m'
    
    az spring-cloud app create --name ${VISITS_SERVICE} --instance-count 1 \
        --memory 2 \
        --jvm-options='-Xms2048m -Xmx2048m'
```

## Create MySQL Database

Create a MySQL database in Azure Database for MySQL.

```bash
    // create mysql server
    az mysql server create --resource-group ${RESOURCE_GROUP} \
     --name ${MYSQL_SERVER_NAME}  --location ${REGION} \
     --admin-user ${MYSQL_SERVER_ADMIN_NAME} \
     --admin-password ${MYSQL_SERVER_ADMIN_PASSWORD} \
     --sku-name GP_Gen5_2 \
     --ssl-enforcement Disabled \
     --version 5.7
    
    // allow access from Azure resources
    az mysql server firewall-rule create --name allAzureIPs \
     --server ${MYSQL_SERVER_NAME} \
     --resource-group ${RESOURCE_GROUP} \
     --start-ip-address 0.0.0.0 --end-ip-address 0.0.0.0
    
    // allow access from your dev machine for testing
    az mysql server firewall-rule create --name devMachine \
     --server ${MYSQL_SERVER_NAME} \
     --resource-group ${RESOURCE_GROUP} \
     --start-ip-address <ip-address-of-your-dev-machine> \
     --end-ip-address <ip-address-of-your-dev-machine>
    
    // increase connection timeout
    az mysql server configuration set --name wait_timeout \
     --resource-group ${RESOURCE_GROUP} \
     --server ${MYSQL_SERVER_NAME} --value 2147483
    
    // SUBSTITUTE values
    mysql -u ${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
     -h ${MYSQL_SERVER_FULL_NAME} -P 3306 -p
    
    Enter password:
    Welcome to the MySQL monitor.  Commands end with ; or \g.
    Your MySQL connection id is 64379
    Server version: 5.6.39.0 MySQL Community Server (GPL)
    
    Copyright (c) 2000, 2018, Oracle and/or its affiliates. All rights reserved.
    
    Oracle is a registered trademark of Oracle Corporation and/or its
    affiliates. Other names may be trademarks of their respective
    owners.
    
    Type 'help;' or '\h' for help. Type '\c' to clear the current input statement.
    
    mysql> CREATE DATABASE petclinic;
    Query OK, 1 row affected (0.10 sec)
    
    mysql> CREATE USER 'root' IDENTIFIED BY 'petclinic';
    Query OK, 0 rows affected (0.11 sec)
    
    mysql> GRANT ALL PRIVILEGES ON petclinic.* TO 'root';
    Query OK, 0 rows affected (1.29 sec)
    
    mysql> CALL mysql.az_load_timezone();
    Query OK, 3179 rows affected, 1 warning (6.34 sec)
    
    mysql> SELECT name FROM mysql.time_zone_name;
    ...
    
    mysql> quit
    Bye
    
    
    az mysql server configuration set --name time_zone \
     --resource-group ${RESOURCE_GROUP} \
     --server ${MYSQL_SERVER_NAME} --value "US/Pacific"
```

## Deploy applications and set environment variables

Deploy microservice applications to Azure.

```bash
    az spring-cloud app deploy --name ${API_GATEWAY} \
        --jar-path ${API_GATEWAY_JAR} \
        --jvm-options='-Xms2048m -Xmx2048m -Dspring.profiles.active=mysql'
    
    
    az spring-cloud app deploy --name ${ADMIN_SERVER} \
        --jar-path ${ADMIN_SERVER_JAR} \
        --jvm-options='-Xms2048m -Xmx2048m -Dspring.profiles.active=mysql'
    
    
    az spring-cloud app deploy --name ${CUSTOMERS_SERVICE} \
        --jar-path ${CUSTOMERS_SERVICE_JAR} \
        --jvm-options='-Xms2048m -Xmx2048m -Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
    
    
    az spring-cloud app deploy --name ${VETS_SERVICE} \
        --jar-path ${VETS_SERVICE_JAR} \
        --jvm-options='-Xms2048m -Xmx2048m -Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
              
    
    az spring-cloud app deploy --name ${VISITS_SERVICE} \
        --jar-path ${VISITS_SERVICE_JAR} \
        --jvm-options='-Xms2048m -Xmx2048m -Dspring.profiles.active=mysql' \
        --env MYSQL_SERVER_FULL_NAME=${MYSQL_SERVER_FULL_NAME} \
              MYSQL_DATABASE_NAME=${MYSQL_DATABASE_NAME} \
              MYSQL_SERVER_ADMIN_LOGIN_NAME=${MYSQL_SERVER_ADMIN_LOGIN_NAME} \
              MYSQL_SERVER_ADMIN_PASSWORD=${MYSQL_SERVER_ADMIN_PASSWORD}
```

```bash
    az spring-cloud app show --name ${API_GATEWAY} | grep url
```

Navigate to the URL provided by the previous command to open the Pet Clinic microservice application.
    
![](./media/petclinic.jpg)

## Working with GitHub Actions

### Prepare secrets in your Key Vault
If you don't have a Key Vault yet, run the following commands to provision a Key Vault:
``` bash
export KEY_VAULT=<your-keyvault-name>
az keyvault create --name ${KEY_VAULT} -g ${RESOURCE_GROUP}
```

Add the MySQL secrets to your Key Vault:
```bash
az keyvault secret set --vault-name ${KEY_VAULT} --name "MYSQL-DATABASE-NAME" --value ${MYSQL_DATABASE_NAME}
az keyvault secret set --vault-name ${KEY_VAULT} --name "MYSQL-SERVER-ADMIN-LOGIN-NAME" --value ${MYSQL_SERVER_ADMIN_LOGIN_NAME}
az keyvault secret set --vault-name ${KEY_VAULT} --name "MYSQL-SERVER-ADMIN-PASSWORD" --value ${MYSQL_SERVER_ADMIN_PASSWORD}
az keyvault secret set --vault-name ${KEY_VAULT} --name "MYSQL-SERVER-FULL-NAME" --value ${MYSQL_SERVER_FULL_NAME}
```

Create a service priciple with enough scope/role to manage your Azure Spring Cloud instance:
```bash
az ad sp create-for-rbac --role contributor --scopes /subscriptions/<SUBSCRIPTION_ID> --sdk-auth
```
With results:
```json
{
    "clientId": "<GUID>",
    "clientSecret": "<GUID>",
    "subscriptionId": "<GUID>",
    "tenantId": "<GUID>",
    "activeDirectoryEndpointUrl": "https://login.microsoftonline.com",
    "resourceManagerEndpointUrl": "https://management.azure.com/",
    "sqlManagementEndpointUrl": "https://management.core.windows.net:8443/",
    "galleryEndpointUrl": "https://gallery.azure.com/",
    "managementEndpointUrl": "https://management.core.windows.net/"
}
```
Add it as a secret to your Key Vault:
```bash
az keyvault secret set --vault-name ${KEY_VAULT} --name "AZURE-CREDENTIALS-FOR-SPRING" --value "<results above>"
```

### Grant access to your Key Vault with Service Principle
To generate a key to access the key vault, execute command below:
```bash
az ad sp create-for-rbac --role contributor --scopes /subscriptions/<SUBSCRIPTION_ID>/resourceGroups/<RESOURCE_GROUP>/providers/Microsoft.KeyVault/vaults/<KEY_VAULT> --sdk-auth
```
Then, follow [the steps here](https://docs.microsoft.com/azure/spring-cloud/spring-cloud-github-actions-key-vault#add-access-policies-for-the-credential) to add access policy for the Service Principle.

In the end, add this service principle as a secrets named "AZURE_CREDENTIALS" in your forked GitHub repo following [the steps here](https://docs.microsoft.com/en-us/azure/spring-cloud/spring-cloud-github-actions-key-vault#add-access-policies-for-the-credential).

### Customize your workflow
Finally, edit the workfolw file `.github/workflows/action.yml` in your forked repo to fill in the names of resource group and Azure Spring Cloud instance you just created:
```yml
env:
  RESOURCE_GROUP: resource-group-name # customize this
  SPRING_CLOUD_SERVICE: azure-spring-cloud-name # customize this
```
After your commited this change, you will see GitHub Actions triggered to build and deploy all the apps in the repo to your Azure Spring Cloud instance.

## Next Steps

In this quickstart, you've deployed an existing Spring microservices app using Azure CLI. To learn more about Azure Spring Cloud, go to:

- [Azure Spring Cloud](https://azure.microsoft.com/en-us/services/spring-cloud/)
- [Azure Spring Cloud docs](https://docs.microsoft.com/en-us/azure/java/)
- [Deploy Spring microservices from scratch](https://github.com/microsoft/azure-spring-cloud-training)
- [Deploy existing Spring microservices](https://github.com/Azure-Samples/azure-spring-cloud)
- [Azure for Java Cloud Developers](https://docs.microsoft.com/en-us/azure/java/)
- [Spring Cloud Azure](https://cloud.spring.io/spring-cloud-azure/)
- [Spring Cloud](https://spring.io/projects/spring-cloud)

## Credits

This Spring microservices sample is forked from 
[spring-petclinic/spring-petclinic-microservices](https://github.com/spring-petclinic/spring-petclinic-microservices) - see [Petclinic README](./README-petclinic.md). 

## Contributing

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
