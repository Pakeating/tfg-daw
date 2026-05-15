# Fase 1: Compilación
FROM ghcr.io/graalvm/native-image-community:21 AS build
WORKDIR /app

# Copiar archivos de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

# Copiar código y compilar a nativo
COPY src ./src
RUN ./mvnw native:compile -Pnative -DskipTests -DskipITs -DskipNativeTests -Dmaven.test.skip=true

# Fase 2: Ejecución
# para desarrollo, incluye ssh, etc
FROM debian:bookworm-slim
# para entorno productivo
#FROM gcr.io/distroless/cc-debian12
WORKDIR /app

# Instalar librerías necesarias para el binario nativo
RUN apt-get update && apt-get install -y zlib1g && rm -rf /var/lib/apt/lists/*

# Copiar el binario desde la fase de build
# NOTA: Asegúrate de que el nombre coincida con el artifactId de tu pom.xml
COPY --from=build /app/target/EurekaServer ./app-bin

EXPOSE 8080
ENTRYPOINT ["./app-bin"]