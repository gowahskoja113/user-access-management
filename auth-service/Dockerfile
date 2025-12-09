# File: auth-service/Dockerfile
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Lưu ý: Tên file .jar phải khớp với tên trong thư mục target sau khi build
COPY target/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "auth-service.jar"]