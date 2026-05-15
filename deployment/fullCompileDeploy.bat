@echo off

:: Compila y despliega toda la infraestructura de INMOPACO en modo nativo usando Docker Compose.

echo ======================================================
echo            Compilando y Desplegando:
echo            INMOPACO - Despliegue Nativo
echo            Running at: %date% %time%
echo ======================================================

::echo Compilando Eureka Server...
::call docker-compose -f deployment/docker-compose.yml build --no-cache eureka-server

:: OPCIONES DE BUILD:
::--no-cache

echo Compilando Admin...
::call docker-compose -f docker-compose.yml build admin-server|| (echo Error en Admin && pause && exit /b)

echo Compilando Config Service...
::call docker-compose -f docker-compose.yml build --no-cache config-service-v3|| (echo Error en Config Service && pause && exit /b)

echo Compilando API Gateway...
::call docker-compose -f docker-compose.yml build  api-gateway-v3|| (echo Error en API Gateway && pause && exit /b)

echo Compilando Libreria de Eventos...
::call docker build -t eventsourcing-base -f ../EventSourcingCommons/Dockerfile .. || (echo Error en EventSourcingCommons && pause && exit /b)

echo Compilando Orchestrator...
::call docker-compose -f docker-compose.yml build  orchestrator || (echo Error en Orchestrator && pause && exit /b)

echo Compilando Auction Service...
::call docker-compose -f docker-compose.yml build  auction-service|| (echo Error en Auction Service && pause && exit /b)

echo Compilando Property Service...
::call docker-compose -f docker-compose.yml build  property-service|| (echo Error en Property Service && pause && exit /b)

echo Compilando AI Service...
::call docker-compose -f docker-compose.yml build  ai-service|| (echo Error en AI Service && pause && exit /b)

echo Compilando BFF...
call docker-compose -f docker-compose.yml build  bff|| (echo Error en BFF && pause && exit /b)

echo Compilando Notification Service...
::call docker-compose -f docker-compose.yml build  notification-service|| (echo Error en Notification && pause && exit /b)

::Limpiar tambien volumenes, etc, por si fuese necesario (ELIMINA PERSISTENCIA DE DATOS)
::echo "Limpiando infraestructura anterior..."
::docker-compose down

echo Levantando contenedores individuales...
call docker-compose -f docker-compose.yml up -d --force-recreate bff
::call docker-compose -f docker-compose.yml up -d --force-recreate orchestrator
::call docker-compose -f docker-compose.yml up -d --force-recreate property-service

::--force-recreate obliga a tumbar los contenedores que ya esten creados de antes, se puede quitar mas adelante...
::echo Levantando infraestructura completa...
::call docker-compose -f docker-compose.yml up -d --force-recreate

echo ======================================================
echo   Despliegue finalizado
echo   Admin:           http://localhost:9090/admin
echo   Queue Dashboard: http://localhost:8282
echo   BFF Swagger:     http://localhost:8083/swagger-ui/index.html
echo   API Gateway:     http://localhost:8080
echo   Adminer:         http://localhost:4000
echo ======================================================