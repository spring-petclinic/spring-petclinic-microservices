# Use a lightweight base image
FROM ubuntu:22.04

# Set non-interactive mode for apt
ENV DEBIAN_FRONTEND=noninteractive

# Define Java and Maven environment variables
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV MAVEN_HOME=/opt/maven
ENV PATH="${MAVEN_HOME}/bin:${JAVA_HOME}/bin:/usr/local/bin:${PATH}"

# Install required packages (excluding nodejs, python3)
RUN apt-get update && apt-get install -y --no-install-recommends \
  ca-certificates curl git unzip zip \
  openjdk-17-jdk \
  docker.io \
  jq sudo && \
  rm -rf /var/lib/apt/lists/*

# Install latest Maven (instead of outdated apt package)
RUN curl -fsSL https://downloads.apache.org/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.tar.gz \
  | tar -xz -C /opt/ && \
  ln -s /opt/apache-maven-3.9.5 /opt/maven

# Install Kubectl
RUN sh -c 'curl -LO "https://dl.k8s.io/release/v1.32.2/bin/linux/amd64/kubectl"' && \
  chmod +x kubectl && mv kubectl /usr/local/bin/

# Install Helm
RUN curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# Install Trivy (for security scanning)
RUN curl -sfL https://aquasecurity.github.io/trivy-repo/deb/public.key | sudo tee -a /etc/apt/trusted.gpg.d/aquasecurity.asc && \
  echo "deb https://aquasecurity.github.io/trivy-repo/deb jammy main" | sudo tee -a /etc/apt/sources.list.d/trivy.list && \
  sudo apt update && sudo apt install -y trivy && \
  rm -rf /var/lib/apt/lists/*

# Install SonarScanner (for code quality analysis)
RUN curl -o /tmp/sonar-scanner.zip -L "https://binaries.sonarsource.com/Distribution/sonar-scanner-cli/sonar-scanner-cli-5.0.1.3006-linux.zip" && \
  unzip /tmp/sonar-scanner.zip -d /opt/ && \
  mv /opt/sonar-scanner-* /opt/sonar-scanner && \
  ln -s /opt/sonar-scanner/bin/sonar-scanner /usr/local/bin/sonar-scanner && \
  rm /tmp/sonar-scanner.zip

# ✅ Check if "docker" group exists before adding it
RUN getent group docker || groupadd -g 999 docker && \
  useradd -m -d /home/jenkins -u 1000 -g docker -s /bin/bash jenkins && \
  echo "jenkins ALL=(ALL) NOPASSWD:ALL" >> /etc/sudoers

USER jenkins
WORKDIR /home/jenkins
CMD ["/bin/bash"]
