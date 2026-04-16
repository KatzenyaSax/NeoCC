FROM eclipse-temurin:21-jdk

WORKDIR /app

# 复制 Maven 构建的 jar 文件
COPY target/sales-*.jar app.jar

# 暴露端口
EXPOSE 8083

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
