# --- Etapa 1: Build ---

#JVM VERSION:
#
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiamos la librería común y la instalamos en el repo local de Maven del contenedor
COPY EventSourcingCommons ./EventSourcingCommons
RUN mvn -f ./EventSourcingCommons/pom.xml clean install -DskipTests

# 2. Ahora preparamos el microservicio
COPY AuctionService ./AuctionService
WORKDIR /app/AuctionService

RUN mvn clean package -DskipNativeBuild=true -DskipTests

# --- Etapa 2: Run ---
# Usamos una imagen JRE (Runtime) mucho más pequeña para la ejecución
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar Tesseract OCR y dependencias necesarias para el procesamiento de imágenes asegurando repositorios compatibles
RUN apk add --no-cache \
    --repository http://dl-cdn.alpinelinux.org/alpine/edge/community \
    --repository http://dl-cdn.alpinelinux.org/alpine/edge/main \
    tesseract-ocr \
    tesseract-ocr-data-spa \
    leptonica \
    libjpeg-turbo \
    libpng \
    tiff \
    giflib
# Creamos un usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/logs && chown -R spring:spring /app
USER spring

# Copiamos el archivo JAR generado en la etapa anterior
COPY --from=build /app/AuctionService/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

##GRAALVM VERSION:
#
## Fase 1: Compilación
#FROM eventsourcing-base AS build
#ENV LANG=C.UTF-8
#ENV LC_ALL=C.UTF-8
#WORKDIR /app
#
## Copiar archivos de Maven
#COPY AuctionService/mvnw ./
#COPY AuctionService/.mvn/ .mvn/
#
## Compilamos
#COPY AuctionService/pom.xml ./AuctionService/
#RUN ./mvnw -f ./AuctionService/pom.xml dependency:go-offline
#
## Copiamos el codigo y compilamos a nativo
#COPY AuctionService/src ./AuctionService/src
#RUN ./mvnw -f ./AuctionService/pom.xml native:compile -Pnative -DskipTests -Dmaven.test.skip=true -Dnative.buildArgs="-Ob -J-XX:ActiveProcessorCount=2"
#
## Fase 2: Ejecución
## para desarrollo, incluye ssh, etc
#FROM debian:bookworm-slim
## para entorno productivo
##FROM gcr.io/distroless/cc-debian12
#WORKDIR /app
#
## Instalar librerías necesarias para el binario nativo
#RUN apt-get update && apt-get install -y --no-install-recommends zlib1g && rm -rf /var/lib/apt/lists/*
#
## Copiar el binario desde la fase de build
## NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
#COPY --from=build /app/AuctionService/target/AuctionService ./app-bin
#
#EXPOSE 8081
#ENTRYPOINT ["./app-bin"]