@echo off
setlocal enabledelayedexpansion

:: Verificar si el archivo .env existe
if not exist .env (
    echo Error: No se encontro el archivo .env
    pause
    exit /b
)

:: Leer el archivo .env y cargar las variables
for /f "usebackq delims=" %%a in (".env") do (
    set "%%a"
)

:: cambiar nombre del servicio aqui
set IMAGE_NAME=%NOTIFICATION%

echo === Iniciando sesion en GHCR para %GH_USER% ===
echo %GH_PAT% | docker login ghcr.io -u %GH_USER% --password-stdin

echo === Construyendo imagen Docker %IMAGE_NAME%===
call docker-compose -f docker-compose.yml build %IMAGE_NAME% || (echo Error en build de %IMAGE_NAME% && pause && exit /b)

echo === Etiquetando y subiendo imagen para GitHub ===
echo --- Subiendo con tag: %TAG%
docker tag %IMAGE_NAME% ghcr.io/%GH_USER%/inmopaco/%IMAGE_NAME%:%TAG%
docker push ghcr.io/%GH_USER%/inmopaco/%IMAGE_NAME%:%TAG%

echo --- Subiendo con tag: latest
docker tag %IMAGE_NAME% ghcr.io/%GH_USER%/inmopaco/%IMAGE_NAME%:latest
docker push ghcr.io/%GH_USER%/inmopaco/%IMAGE_NAME%:latest

echo === Proceso finalizado ===
pause