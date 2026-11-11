# Ansible Deployment for Spring Petclinic & Centralized Logging (ELK)

This directory contains production-ready Ansible playbooks and roles to automate the deployment and configuration of:
1. **ELK Stack (Elasticsearch, Logstash, Kibana)** for centralized logging.
2. **Filebeat** log shipper on application nodes.
3. **Docker Engine & Docker Compose**.
4. **Spring Petclinic Microservices Stack**.

## Directory Layout

```text
ansible/
├── ansible.cfg                    # Ansible default settings
├── inventory/
│   ├── hosts.ini                  # Target hosts and inventory groups
│   └── group_vars/
│       ├── all.yml                # Shared variables (versions, ports)
│       ├── elk.yml                # ELK cluster and resource sizing variables
│       └── petclinic.yml          # App server and log harvesting settings
├── site.yml                       # Full deployment playbook (ELK + App + Filebeat)
├── deploy-elk.yml                 # Standalone ELK stack deployment
├── deploy-petclinic.yml           # Standalone Petclinic application deployment
└── roles/
    ├── docker/                    # Installs Docker & Compose plugin
    ├── elasticsearch/             # Provisions Elasticsearch single-node / cluster
    ├── logstash/                  # Configures Logstash pipeline & ports
    ├── kibana/                    # Sets up Kibana web interface
    ├── filebeat/                  # Configures Filebeat log shipper
    └── petclinic/                 # Deploys Spring Petclinic microservices
```

## Quick Start

### 1. Configure Inventory
Update `inventory/hosts.ini` with your server IPs:
```ini
[elk_servers]
elk-node-1 ansible_host=192.168.1.100 ansible_user=ubuntu

[petclinic_servers]
petclinic-app-1 ansible_host=192.168.1.101 ansible_user=ubuntu
```

### 2. Deploy Everything
Run the master playbook:
```bash
ansible-playbook -i inventory/hosts.ini site.yml
```

### 3. Deploy Only the ELK Logging Stack
```bash
ansible-playbook -i inventory/hosts.ini deploy-elk.yml
```

### 4. Deploy Only Petclinic & Filebeat
```bash
ansible-playbook -i inventory/hosts.ini deploy-petclinic.yml
```

## Verifying Deployment
- **Kibana Web UI:** `http://<elk-server-ip>:5601`
- **Elasticsearch Cluster Health:** `curl http://<elk-server-ip>:9200/_cluster/health`
- **Petclinic Frontend:** `http://<app-server-ip>:8080`

