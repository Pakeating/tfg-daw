# --- Etapa 1: Build ---

##JVM VERSION:
##
# FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
# ENV LANG=C.UTF-8
# ENV LC_ALL=C.UTF-8
# WORKDIR /app

# # Compilamos el microservicio
# COPY NotificationService ./NotificationService
# WORKDIR /app/NotificationService

# RUN mvn clean package -DskipNativeBuild=true -DskipTests -Dproc:full

# # --- Etapa 2: Run ---
# # Usamos una imagen JRE (Runtime) mucho más pequeña para la ejecución
# FROM eclipse-temurin:21-jre-alpine
# ENV LANG=C.UTF-8
# ENV LC_ALL=C.UTF-8
# WORKDIR /app

# # Creamos un usuario no root por seguridad
# RUN addgroup -S spring && adduser -S spring -G spring

# # Volumen para el archivo de usuarios de Telegram
# RUN mkdir -p /app/data && \
#     echo "[]" > /app/data/users.json && \
#     chown -R spring:spring /app
# USER spring

# # Copiamos el archivo JAR generado en la etapa anterior
# COPY --from=build /app/NotificationService/target/*.jar app.jar

# EXPOSE 8086
# ENTRYPOINT ["java", "-jar", "app.jar"]

#GRAALVM VERSION:

# Fase 1: Compilación
FROM eventsourcing-base AS build
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

# Copiar archivos de Maven
COPY NotificationService/mvnw ./
COPY NotificationService/.mvn/ .mvn/

# Compilamos
COPY NotificationService/pom.xml ./NotificationService/
RUN ./mvnw -f ./NotificationService/pom.xml dependency:go-offline

# Copiamos el codigo y compilamos a nativo
COPY NotificationService/src ./NotificationService/src
RUN ./mvnw -f ./NotificationService/pom.xml native:compile -Pnative -DskipTests -Dmaven.test.skip=true -Dnative.buildArgs="-Ob"

# Fase 2: Ejecución
# para desarrollo, incluye ssh, etc
FROM debian:bookworm-slim
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
# para entorno productivo
#FROM gcr.io/distroless/cc-debian12
WORKDIR /app

# Instalar librerías necesarias para el binario nativo
RUN apt-get update && apt-get install -y --no-install-recommends zlib1g && rm -rf /var/lib/apt/lists/*

# Copiar el binario desde la fase de build
# NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
COPY --from=build /app/NotificationService/target/NotificationService ./app-bin

EXPOSE 8086
ENTRYPOINT ["./app-bin"]