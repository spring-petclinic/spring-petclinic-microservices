📋 TÓM TẮT STEP 0: BUILD VÀ PUSH IMAGES LÊN DOCKER HUB (UPDATED)
🎯 Mục tiêu:
Tạo Dockerfile cho 8 services và push images với tag latest lên Docker Hub.
📁 8 Services đã có Dockerfile (create từ script: create-dockerfiles.sh):
✅ spring-petclinic-genai-service
✅ spring-petclinic-vets-service  
✅ spring-petclinic-visits-service
✅ spring-petclinic-admin-server
✅ spring-petclinic-api-gateway
✅ spring-petclinic-config-server
✅ spring-petclinic-customers-service
✅ spring-petclinic-discovery-server
✅ Đã hoàn thành:

Dockerfiles được tạo trong từng thư mục service
Base image: eclipse-temurin:17-jre-alpine
Docker Hub credentials đã setup trong file .env

🚀 Bước tiếp theo:
1. Verify các Dockerfiles
bash# Check tất cả Dockerfiles đã đúng base image
for dir in spring-petclinic-*/; do
    if [ -f "$dir/Dockerfile" ]; then
        echo "$dir: $(head -1 $dir/Dockerfile)"
    fi
done
2. Build và Push Images
bash# Chạy script build đã tạo
./build-final.sh

# Hoặc build manual
source .env
echo $DOCKER_HUB_TOKEN | docker login -u $DOCKER_HUB_USERNAME --password-stdin

# Build từng service
for service in spring-petclinic-*/; do
    service_name=$(basename $service)
    cd $service
    mvn clean package -DskipTests
    docker build -t $DOCKER_HUB_USERNAME/$service_name:latest .
    docker push $DOCKER_HUB_USERNAME/$service_name:latest
    cd ..
done
3. Verify Results
bash# Check local images
docker images | grep mikenam

# Check on Docker Hub
# https://hub.docker.com/r/mikenam/spring-petclinic-admin-server
4. Cleanup (sau khi push xong)
bash# Remove local images để tiết kiệm dung lượng
docker images | grep mikenam | awk '{print $1":"$2}' | xargs docker rmi
docker system prune -f
📋 Dockerfile Template đã sử dụng:
dockerfileFROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/spring-petclinic-[service-name]-*.jar [service-name].jar

EXPOSE [PORT]

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "[service-name].jar"]
🔧 Port mapping cho từng service:
- admin-server: 9090
- api-gateway: 8080
- config-server: 8888
- customers-service: 8081
- discovery-server: 8761
- genai-service: 8084
- vets-service: 8083
- visits-service: 8082
⚠️ Lưu ý cho teammates:

Base image: PHẢI dùng eclipse-temurin:17-jre-alpine (not openjdk)
Maven build: Chạy mvn clean package -DskipTests trước khi build Docker
Docker Hub login: Cần có access token, không dùng password
File .env: Không commit vào git (thêm vào .gitignore)
Cleanup: Clean local images sau khi push để tiết kiệm dung lượng

🎯 Expected Output:
8 repositories trên Docker Hub với tag latest:
- mikenam/spring-petclinic-admin-server:latest
- mikenam/spring-petclinic-api-gateway:latest
- mikenam/spring-petclinic-config-server:latest
- mikenam/spring-petclinic-customers-service:latest
- mikenam/spring-petclinic-discovery-server:latest
- mikenam/spring-petclinic-genai-service:latest
- mikenam/spring-petclinic-vets-service:latest
- mikenam/spring-petclinic-visits-service:latest

✅ Status: Dockerfiles created ✓ | Ready for build & push ✓**