@echo off

:: Despliega toda la infraestructura.

echo ======================================================
echo            Compilando CONFIG y Desplegando todo:
echo            INMOPACO - Despliegue Nativo
echo ======================================================

echo Levantando infraestructura completa...
call docker-compose -f docker-compose.yml up -d --force-recreate

echo ======================================================
echo   Despliegue finalizado
echo   Admin:           http://localhost:9090/admin
echo   Queue Dashboard: http://localhost:8282
echo   API Gateway:     http://localhost:8080
echo ======================================================