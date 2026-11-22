variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project" {
  type    = string
  default = "petclinic"
}

variable "cluster_name" {
  description = "Must equal eks_cluster_name used in the bootstrap stack (the Jenkins role is scoped to this name)"
  type        = string
  default     = "petclinic-eks"
}

variable "kubernetes_version" {
  description = "EKS Kubernetes version. 1.35 has standard support until 27 March 2027"
  type        = string
  default     = "1.35"
}

variable "state_bucket" {
  description = "Name of the Terraform state bucket (same value as in backend.hcl). Used to read the bootstrap stack outputs"
  type        = string
}

variable "my_ip_cidr" {
  description = "Your public IP as a /32 (curl -s https://checkip.amazonaws.com). Allowed to reach the Kubernetes API"
  type        = string

  validation {
    condition     = can(cidrhost(var.my_ip_cidr, 0)) && endswith(var.my_ip_cidr, "/32")
    error_message = "Use a single address in CIDR form, for example 203.0.113.10/32."
  }
}

variable "extra_api_cidrs" {
  description = "Optional additional /32 or CIDR ranges allowed to reach the Kubernetes API"
  type        = list(string)
  default     = []
}

variable "app_namespace" {
  description = "Namespace the Jenkins role may deploy into"
  type        = string
  default     = "petclinic"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}

variable "az_count" {
  type    = number
  default = 2
}

variable "node_instance_types" {
  description = "Several similar types improve Spot availability"
  type        = list(string)
  default     = ["t3.large", "t3a.large"]
}

variable "node_capacity_type" {
  type    = string
  default = "SPOT"

  validation {
    condition     = contains(["SPOT", "ON_DEMAND"], var.node_capacity_type)
    error_message = "Use SPOT or ON_DEMAND."
  }
}

variable "node_min" {
  type    = number
  default = 1
}

variable "node_desired" {
  type    = number
  default = 2
}

variable "node_max" {
  type    = number
  default = 3
}

variable "node_disk_gb" {
  type    = number
  default = 30
}

variable "enabled_cluster_log_types" {
  description = "Control-plane logs sent to CloudWatch. Empty keeps cost minimal; add audit and authenticator in Phase 10"
  type        = list(string)
  default     = []
}
