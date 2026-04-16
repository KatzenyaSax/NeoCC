FROM eclipse-temurin:21-jdk

WORKDIR /app

# 复制 Maven 构建的 jar 文件
COPY target/auth-*.jar app.jar

# 暴露端口
EXPOSE 8085

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
