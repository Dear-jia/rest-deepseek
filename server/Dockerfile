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
COPY index.html css js assets /app/public/

ENV PUBLIC_DIR=/app/public

EXPOSE 8080
CMD ["sh", "-c", "java -XX:MaxRAMPercentage=60 -jar /app/app.jar"]
