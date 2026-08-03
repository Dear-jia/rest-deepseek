# ============================================================
# 文峰小馆 · 多阶段构建镜像
# 阶段一：用 Maven 在镜像内编译 Spring Boot 应用（Render 等平台无需本地 jar）
# 阶段二：精简 JRE 运行环境
# ============================================================

FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

ENV MAVEN_OPTS="-Xmx1024m"

COPY server/pom.xml ./pom.xml
RUN mvn -q -B -DskipTests dependency:go-offline || true

COPY server/src ./src
RUN mvn -q -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /build/target/wenfeng-kitchen-0.1.0.jar /app/app.jar
# 注意：多源 COPY 会把目录内容平铺，必须逐个按原名拷贝，保持 css/js/assets 目录结构
COPY index.html /app/public/index.html
COPY css /app/public/css
COPY js /app/public/js
COPY assets /app/public/assets

ENV PUBLIC_DIR=/app/public
ENV TZ=Asia/Shanghai

EXPOSE 8080
# 免费实例内存为 512MB，JVM 堆上限设为 30%，避免 OOM 被杀
CMD ["sh", "-c", "java -Duser.timezone=Asia/Shanghai -XX:MaxRAMPercentage=30 -jar /app/app.jar"]
