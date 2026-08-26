#!/bin/bash
#
# Setup de la base de datos - Ronda Backend (Linux / Mac)
# Equivalente a setup-db.bat

set -u

cd "$(dirname "$0")"

echo "==============================================="
echo "  Setup de la base de datos - Ronda Backend"
echo "==============================================="
echo

if ! command -v mysql >/dev/null 2>&1; then
    echo "[ERROR] No se encontro el cliente 'mysql' en el PATH."
    echo
    echo "  Ubuntu/Debian : sudo apt install mysql-client"
    echo "  macOS         : brew install mysql-client"
    echo
    exit 1
fi

echo "Cliente MySQL encontrado: $(command -v mysql)"
echo

echo "Se va a crear:"
echo "  - la base de datos  \"ronda\""
echo "  - el usuario        \"ronda\" con password \"ronda\""
echo
echo "A continuacion se te va a pedir la password de ROOT de MySQL."
echo

if ! mysql -u root -p -h 127.0.0.1 -P 3306 -e "source setup-db.sql"; then
    echo
    echo "[ERROR] Fallo la ejecucion del script."
    echo "Causa mas comun: la password de root es incorrecta."
    exit 1
fi

echo
echo "==============================================="
echo "  Listo."
echo "==============================================="
echo
echo "Ya podes levantar el backend. Desde esta misma carpeta:"
echo
echo "  ./mvnw spring-boot:run"
echo
echo "Y despues abri en el navegador:  http://localhost:8081/api/health"
