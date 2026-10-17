# ==========================================
# 阶段 1：构建环境 (多阶段构建，支持直接源码构建)
# ==========================================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 复制 pom.xml 先下载依赖 (利于 Docker 镜像缓存)
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 复制源码并执行构建
COPY src ./src
RUN mvn clean package -DskipTests=true

# ==========================================
# 阶段 2：轻量级运行时环境 (Alpine JRE)
# ==========================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="congwu.lei"

# 安装时区数据并设置为中国上海时区 (避免日历排期出现 UTC 偏差)
RUN apk add --no-cache tzdata \
    && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

# 创建非 root 安全运行用户
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# 创建持久化数据和日志目录并授权
RUN mkdir -p /app/temp/data /app/temp/logs \
    && chown -R spring:spring /app

# 从构建阶段复制 jar 包
COPY --from=builder /build/target/memory-line-1.0.0.jar /app/app.jar
RUN chown spring:spring /app/app.jar

# 切换为低权限非 root 用户运行
USER spring:spring

EXPOSE 8088

# 默认 JVM 参数与时区
ENV JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8 -Duser.timezone=Asia/Shanghai"
ENV SPRING_PROFILES_ACTIVE="prod"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
