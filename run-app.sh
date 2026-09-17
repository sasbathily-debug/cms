#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/app"

if [ ! -f .env ]; then
  echo "Fichier .env introuvable dans app/. Copie app/.env.example en app/.env et remplis les valeurs." >&2
  exit 1
fi

../mvnw spring-boot:run


# ./mvnw -q install -DskipTests
# ./mvnw spring-boot:run