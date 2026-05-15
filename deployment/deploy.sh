#!/bin/bash

echo "🚀 Iniciando compilación nativa y despliegue..."

# Levantar infraestructura (esto disparará los Dockerfiles)
docker-compose up -d --build

echo "✅ Sistema levantado. Revisa los logs con: docker-compose logs -f"