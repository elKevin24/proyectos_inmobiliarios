# Multi-stage build para optimizar el tamaño de la imagen
# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
# Descargar dependencias (se cachea si no cambia pom.xml)
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar aplicación (skip tests para build más rápido)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Metadatos
LABEL maintainer="kevin@inmobiliaria.com"
LABEL version="1.0.0"
LABEL description="Backend SaaS Multi-tenant Gestión de Terrenos"

# Crear usuario no-root para seguridad
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copiar JAR desde stage de build
COPY --from=build /app/target/*.jar app.jar

# Cambiar ownership
RUN chown -R spring:spring /app

# Cambiar a usuario no-root
USER spring:spring

# Exponer puerto
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Variables de entorno por defecto
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# Ejecutar aplicación
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
