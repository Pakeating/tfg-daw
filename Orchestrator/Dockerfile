

#JVM VERSION:
# --- Etapa 1: Build ---
#FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
#WORKDIR /app
#
## 1. Copiamos la librería común y la instalamos en el repo local de Maven del contenedor
#COPY EventSourcingCommons ./EventSourcingCommons
#RUN mvn -f ./EventSourcingCommons/pom.xml clean install -DskipTests
#
## 2. Ahora preparamos el microservicio
#COPY Orchestrator ./Orchestrator
#WORKDIR /app/Orchestrator
#
#RUN mvn clean package -DskipNativeBuild=true -DskipTests -Dproc:full
#
## --- Etapa 2: Run ---
## Usamos una imagen JRE (Runtime) mucho más pequeña para la ejecución
#FROM eclipse-temurin:21-jre-alpine
#WORKDIR /app
#
## Creamos un usuario no root por seguridad
#RUN addgroup -S spring && adduser -S spring -G spring
#USER spring
#
## Copiamos el archivo JAR generado en la etapa anterior
#COPY --from=build /app/Orchestrator/target/*.jar app.jar
#
#EXPOSE 8082
#ENTRYPOINT ["java", "-jar", "app.jar"]

##GRAALVM VERSION:
#
# Fase 1: Compilación
FROM eventsourcing-base AS build
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

# Copiar archivos de Maven
COPY Orchestrator/mvnw ./
COPY Orchestrator/.mvn/ .mvn/

# Compilamos
COPY Orchestrator/pom.xml ./Orchestrator/
RUN ./mvnw -f ./Orchestrator/pom.xml dependency:go-offline

# Copiamos el codigo y compilamos a nativo
COPY Orchestrator/src ./Orchestrator/src
RUN ./mvnw -f ./Orchestrator/pom.xml native:compile -Pnative -DskipTests -Dmaven.test.skip=true -Dnative.buildArgs="-Ob"

# Fase 2: Ejecución
# para desarrollo, incluye ssh, etc
FROM debian:bookworm-slim
# para entorno productivo
#FROM gcr.io/distroless/cc-debian12
WORKDIR /app

# Instalar librerías necesarias para el binario nativo
RUN apt-get update && apt-get install -y  --no-install-recommends zlib1g && rm -rf /var/lib/apt/lists/*

# Copiar el binario desde la fase de build
# NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
COPY --from=build /app/Orchestrator/target/Orchestrator ./app-bin

EXPOSE 8082
ENTRYPOINT ["./app-bin"]