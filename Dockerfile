# ============================================================
# Dockerfile — BTG Pactual Fondos API
# Multi-stage build: compila con Maven y corre con JRE slim
# ============================================================

# ── Etapa 1: Build ──────────────────────────────────────────
FROM amazoncorretto:17-alpine AS builder

WORKDIR /app

# Copiar solo el pom primero para aprovechar la cache de capas
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw

# Descargar dependencias (cacheado si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -q

# Copiar fuentes y compilar
COPY src/ src/
RUN ./mvnw clean package -DskipTests -q

# ── Etapa 2: Runtime ────────────────────────────────────────
FROM amazoncorretto:17-alpine AS runtime

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app

# Copiar solo el JAR del stage de build
COPY --from=builder /app/target/*.jar app.jar

# Cambiar propietario
RUN chown appuser:appgroup app.jar
USER appuser

# Puerto de la aplicación
EXPOSE 8080

# Health check para App Runner / ECS
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Variables de entorno con defaults (se sobreescriben en AWS)
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
