# --- Etapa 1: Build ---

##JVM VERSION:
##
FROM maven:3.9.5-eclipse-temurin-21-alpine AS build
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

# 2. Ahora preparamos el microservicio
COPY BFF ./BFF
WORKDIR /app/BFF

RUN mvn clean package -DskipNativeBuild=true -DskipTests -Dproc:full

# --- Etapa 2: Run ---
# Usamos una imagen JRE (Runtime) mucho más pequeña para la ejecución
FROM eclipse-temurin:21-jre-alpine
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

# Creamos un usuario no root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

# Copiamos el archivo JAR generado en la etapa anterior
COPY --from=build /app/BFF/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

##GRAALVM VERSION:
#
## Fase 1: Compilación
#FROM ghcr.io/graalvm/native-image-community:21 AS build
#FROM eventsourcing-base AS build
#ENV LANG=C.UTF-8
#ENV LC_ALL=C.UTF-8
#WORKDIR /app
#
## Copiar archivos de Maven
#COPY BFF/mvnw ./
#COPY BFF/.mvn/ .mvn/
#
###SI FUNCIONA LA LIBRERIA COMUN NO ES NECESARIO
### Copiamos la carpeta de la librería común y la instalamos en el .m2 local del contenedor para que esté disponible
##COPY EventSourcingCommons ./EventSourcingCommons
##RUN ./mvnw -f ./EventSourcingCommons/pom.xml clean install -DskipTests
#
## Compilamos
#COPY BFF/pom.xml ./BFF/
#RUN ./mvnw -f ./BFF/pom.xml dependency:go-offline
#
## Copiamos el codigo y compilamos a nativo
#COPY BFF/src ./BFF/src
#RUN ./mvnw -f ./BFF/pom.xml native:compile -Pnative -DskipTests -Dmaven.test.skip=true -Dnative.buildArgs="-Ob" #Este ultimo hace compilados mas rapidos pero optimiza menos...
#
## Fase 2: Ejecución
## para desarrollo, incluye ssh, etc
#FROM debian:bookworm-slim
#ENV LANG=C.UTF-8
#ENV LC_ALL=C.UTF-8
## para entorno productivo
##FROM gcr.io/distroless/cc-debian12
#WORKDIR /app
#
## Instalar librerías necesarias para el binario nativo. El noinstall-recommends es apra evitar isntalar cosas innecesarias, esta en pruebas
#RUN apt-get update && apt-get install -y --no-install-recommends zlib1g && rm -rf /var/lib/apt/lists/*
#
## Copiar el binario desde la fase de build
## NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
#COPY --from=build /app/BFF/target/BFF ./app-bin
#
#EXPOSE 8081
#ENTRYPOINT ["./app-bin"]