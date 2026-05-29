# 使用官方 OpenJDK 17 镜像作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制构建好的 jar 文件（假设打包后叫 app.jar）
COPY target/*.jar app.jar

# 暴露 Spring Boot 默认端口（如 8080）
EXPOSE 8081

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]