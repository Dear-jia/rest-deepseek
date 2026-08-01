FROM eclipse-temurin:17-jre

WORKDIR /app

# 复制可执行 jar 与前台静态页面（构建上下文为仓库根目录）
COPY server/target/wenfeng-kitchen-0.1.0.jar /app/app.jar
COPY index.html css js assets /app/public/

ENV PUBLIC_DIR=/app/public
ENV JAVA_OPTS="-Xms256m -Xmx512m"

RUN mkdir -p /app/data
VOLUME ["/app/data"]
EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
