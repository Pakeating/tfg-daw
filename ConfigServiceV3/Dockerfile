# Fase 1: Compilación
FROM ghcr.io/graalvm/native-image-community:21 AS build
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /app

# Copiar archivos de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copiar código y compilar a nativo
COPY src ./src
RUN ./mvnw native:compile -Pnative -DskipTests -DskipITs -DskipNativeTests -Dmaven.test.skip=true -Dnative.buildArgs="-Ob"

# Fase 2: Ejecución
# para desarrollo, incluye ssh, etc
FROM debian:bookworm-slim
# para entorno productivo
#FROM gcr.io/distroless/cc-debian12
WORKDIR /app

# Instalar librerías necesarias para el binario nativo
RUN apt-get update && apt-get install -y --no-install-recommends zlib1g curl && rm -rf /var/lib/apt/lists/*

# Copiar el binario desde la fase de build
# NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
COPY --from=build /app/target/ConfigServiceV3 ./app-bin

EXPOSE 8080
ENTRYPOINT ["./app-bin"]