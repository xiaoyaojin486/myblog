FROM eclipse-temurin:21-jre
WORKDIR /app
ENV TZ=Asia/Shanghai
COPY app.jar app.jar
EXPOSE 8080
# JAVA_OPTS 从 .env 注入，方便在小内存服务器上调堆大小
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
