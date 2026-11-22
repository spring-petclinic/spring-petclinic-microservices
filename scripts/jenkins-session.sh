#!/usr/bin/env bash
# Start/stop/inspect the Jenkins server and print the values that change when its public IP changes.
# Usage (Git Bash, after: export AWS_PROFILE=petclinic):
#   bash scripts/jenkins-session.sh start     # start, wait for SSM, print IP + webhook URL + tunnel command
#   bash scripts/jenkins-session.sh tunnel    # open the Jenkins UI tunnel -> browse http://localhost:8080 (keep window open)
#   bash scripts/jenkins-session.sh tunnel 8081   # same, on another local port
#   bash scripts/jenkins-session.sh shell     # open a shell on the server
#   bash scripts/jenkins-session.sh status
#   bash scripts/jenkins-session.sh stop
set -euo pipefail
export MSYS_NO_PATHCONV=1

REGION="${AWS_REGION:-ap-south-1}"
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ID="${JENKINS_INSTANCE_ID:-$(cd "${ROOT}/infra/bootstrap" && terraform output -raw jenkins_instance_id | tr -d '\r')}"

state() { aws ec2 describe-instances --instance-ids "${ID}" --region "${REGION}" \
            --query 'Reservations[0].Instances[0].State.Name' --output text | tr -d '\r'; }
ip()    { aws ec2 describe-instances --instance-ids "${ID}" --region "${REGION}" \
            --query 'Reservations[0].Instances[0].PublicIpAddress' --output text | tr -d '\r'; }

show() {
  local st addr
  st="$(state)"; addr="$(ip)"
  echo "Instance : ${ID}"
  echo "State    : ${st}"
  if [ "${st}" = "running" ] && [ "${addr}" != "None" ]; then
    echo "Public IP: ${addr}   (changes on every start)"
    echo
    echo "GitHub webhook payload URL : http://${addr}:8080/github-webhook/"
    echo "Jenkins URL (Manage Jenkins > System) : http://${addr}:8080/"
    echo "SSM shell  : aws ssm start-session --target ${ID} --region ${REGION}"
    echo "UI tunnel  : aws ssm start-session --target ${ID} --region ${REGION} --document-name AWS-StartPortForwardingSession --parameters \"portNumber=8080,localPortNumber=8080\""
    echo "Then browse: http://localhost:8080"
  fi
}

case "${1:-status}" in
  start)
    aws ec2 start-instances --instance-ids "${ID}" --region "${REGION}" >/dev/null
    echo "Starting..."
    aws ec2 wait instance-running --instance-ids "${ID}" --region "${REGION}"
    echo "Running. Waiting for Session Manager (up to 3 minutes)..."
    for _ in $(seq 1 18); do
      ping="$(aws ssm describe-instance-information --region "${REGION}" \
              --filters "Key=InstanceIds,Values=${ID}" \
              --query 'InstanceInformationList[0].PingStatus' --output text 2>/dev/null | tr -d '\r' || true)"
      [ "${ping}" = "Online" ] && break
      sleep 10
    done
    echo "SSM      : ${ping:-unknown}"
    show
    echo
    echo "Next: update the GitHub webhook URL, then (EKS sessions) run terraform apply in infra/eks."
    ;;
  stop)
    aws ec2 stop-instances --instance-ids "${ID}" --region "${REGION}" >/dev/null
    echo "Stopping ${ID}. Remember: destroy the EKS stack first if the cluster is up (cd infra/eks && terraform destroy)."
    ;;
  tunnel)
    echo "Jenkins UI: http://localhost:${2:-8080}   (keep this window open; Ctrl+C to close)"
    aws ssm start-session --target "${ID}" --region "${REGION}" \
      --document-name AWS-StartPortForwardingSession \
      --parameters "portNumber=8080,localPortNumber=${2:-8080}"
    ;;
  shell)
    aws ssm start-session --target "${ID}" --region "${REGION}"
    ;;
  status) show ;;
  *) echo "usage: $0 start|stop|status|tunnel [port]|shell" >&2; exit 1 ;;
esac
