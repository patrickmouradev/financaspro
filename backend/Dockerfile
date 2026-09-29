# ============================================================
# Dockerfile — Backend Spring Boot 3 (Java 21)
# Multi-stage build para imagem leve de produção
# ============================================================

# Stage 1: Build com Maven e OpenJDK 21
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia código-fonte e realiza o empacotamento
COPY src ./src
RUN mvn package -DskipTests -B

# Stage 2: Runtime com JRE 21 leve
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Adiciona usuário não-root para segurança
RUN addgroup -S financaspro && adduser -S financaspro -G financaspro
USER financaspro:financaspro

# Copia o artefato .jar compilado
COPY --from=builder /app/target/financaspro-backend-1.0.0-SNAPSHOT.jar app.jar

# Exposição da porta (HML: 8087 / PRD: 8088 via env)
EXPOSE 8087 8088

# Comando de inicialização
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
