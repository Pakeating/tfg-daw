@echo off
setlocal enabledelayedexpansion

:: Verificar si el archivo .env existe
if not exist .env (
    echo Error: No se encontro el archivo .env
    pause
    exit /b
)

:: Leer el archivo .env y cargar las variables de credenciales
for /f "usebackq delims=" %%a in (".env") do (
    set "%%a"
)

:: Verificar si el archivo upload2ghcr.env existe
if not exist upload2ghcr.env (
    echo Error: No se encontro el archivo upload2ghcr.env
    pause
    exit /b
)

echo === Iniciando sesion en GHCR para %GH_USER% ===
echo %GH_PAT% | docker login ghcr.io -u %GH_USER% --password-stdin

:: Leer cada linea del archivo de imagenes y procesar
for /f "usebackq delims=" %%a in ("upload2ghcr.env") do (
    set "line=%%a"
    for /f "tokens=1,2 delims==" %%i in ("!line!") do (
        set "key=%%i"
        set "value=%%j"
        if not "!key!"=="" (
            set "IMAGE_NAME=!value!"
            if defined IMAGE_NAME (
                echo.
                echo ==== Procesando !IMAGE_NAME! ====
                call docker-compose -f docker-compose.yml build !IMAGE_NAME! || (echo Error en build de !IMAGE_NAME! && pause && exit /b)

                echo --- Subiendo con tag: %TAG%
                docker tag !IMAGE_NAME! ghcr.io/%GH_USER%/inmopaco/!IMAGE_NAME!:%TAG%
                docker push ghcr.io/%GH_USER%/inmopaco/!IMAGE_NAME!:%TAG%

                echo --- Subiendo con tag: latest
                docker tag !IMAGE_NAME! ghcr.io/%GH_USER%/inmopaco/!IMAGE_NAME!:latest
                docker push ghcr.io/%GH_USER%/inmopaco/!IMAGE_NAME!:latest

                echo ==== !IMAGE_NAME! completado ====
            )
        )
    )
)

echo === Proceso finalizado ===
pause