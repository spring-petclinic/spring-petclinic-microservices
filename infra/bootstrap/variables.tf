variable "aws_region" {
  description = "AWS region for every resource in this stack"
  type        = string
  default     = "ap-south-1"
}

variable "project" {
  description = "Prefix used in resource names"
  type        = string
  default     = "petclinic"
}

variable "services" {
  description = "Deployable services; one ECR repository each (genai-service is excluded on purpose)"
  type        = list(string)
  default = [
    "config-server",
    "discovery-server",
    "api-gateway",
    "customers-service",
    "visits-service",
    "vets-service",
    "admin-server",
  ]
}

variable "notification_email" {
  description = "Email address that receives Jenkins success/failure messages (must confirm the SNS subscription)"
  type        = string
}

variable "jenkins_instance_type" {
  description = "Jenkins controller size. t3.large is comfortable; t3.medium works but is slow and tight on memory"
  type        = string
  default     = "t3.large"
}

variable "jenkins_volume_gb" {
  description = "Root volume size in GiB (Jenkins home, Maven cache, Docker images)"
  type        = number
  default     = 30
}

variable "jenkins_ingress_cidrs" {
  description = "CIDRs allowed to reach Jenkins on port 8080. Empty (default) = no inbound access; use SSM port forwarding. Add GitHub webhook CIDRs in Phase 3"
  type        = list(string)
  default     = []

  validation {
    condition     = alltrue([for c in var.jenkins_ingress_cidrs : can(cidrhost(c, 0))])
    error_message = "Every entry must be a valid CIDR such as 203.0.113.10/32."
  }
}

variable "use_elastic_ip" {
  description = "Attach an Elastic IP so the Jenkins address (and GitHub webhook URL) survives stop/start. Billed hourly while allocated"
  type        = bool
  default     = false
}

variable "eks_cluster_name" {
  description = "Name of the EKS cluster created in Phase 4 (used to scope the eks:DescribeCluster permission)"
  type        = string
  default     = "petclinic-eks"
}

variable "kubectl_minor" {
  description = "kubectl minor version to install; keep within one minor version of the EKS control plane"
  type        = string
  default     = "1.35"
}

variable "ecr_keep_images" {
  description = "Number of most recent images kept per repository"
  type        = number
  default     = 10
}
