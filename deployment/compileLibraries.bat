@echo off

echo ======================================================
echo            Compilando Librerias Comunes:
echo            INMOPACO - Compilacion Nativa
echo ======================================================
echo Compilando Libreria de Eventos...
call docker build -t eventsourcing-base -f ../EventSourcingCommons/Dockerfile .. || (echo Error en EventSourcingCommons && pause && exit /b)

