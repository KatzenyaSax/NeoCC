FROM eclipse-temurin:21-jdk

WORKDIR /app

# 复制 Maven 构建的 jar 文件
COPY target/gateway-*.jar app.jar

# 复制配置文件到 classpath
COPY src/main/resources/ /app/src/main/resources/

# 暴露端口
EXPOSE 8086

# 启动命令 - 使用docker profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=docker", "app.jar"]
