
# Imagen base para no compilar la libreria en repetidas ocasiones
FROM ghcr.io/graalvm/native-image-community:21
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
WORKDIR /build

COPY EventSourcingCommons/mvnw ./
COPY EventSourcingCommons/.mvn/ .mvn/

COPY EventSourcingCommons/ ./EventSourcingCommons/

RUN chmod +x mvnw && ./mvnw -f ./EventSourcingCommons/pom.xml clean install -DskipTests

#para limpiar el código fuente de la librería que ya está instalada en el .m2 interno
RUN rm -rf /build/EventSourcingCommons