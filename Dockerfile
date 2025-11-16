# ===========================
#   STAGE 1 : Build with Maven
# ===========================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiar POM y descargar dependencias (cache)
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn -q clean package -DskipTests

# ===========================
#   STAGE 2 : Run App
# ===========================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el JAR desde el stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Puerto asignado por Render
ENV PORT=8083

# Variables necesarias para DB, JWT y RabbitMQ
ENV POSTGRES_URL=""
ENV POSTGRES_USER=""
ENV POSTGRES_PASSWORD=""

# Entrypoint de Spring Boot usando el puerto dinámico
ENTRYPOINT ["sh", "-c", "java -jar -Dserver.port=${PORT} app.jar"]
