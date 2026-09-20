#!/bin/bash

echo "======================================================="
echo "  Ronda Backend - Arranque rapido (macOS / Linux)"
echo "======================================================="
echo

# Ir al directorio del script
cd "$(dirname "$0")"

# --- 1. Configurar ADB reverse (Soporta emuladores y dispositivos físicos USB) ---
ADB_CMD=""

if command -v adb &> /dev/null; then
    ADB_CMD="adb"
elif [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB_CMD="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
    ADB_CMD="$HOME/Android/Sdk/platform-tools/adb"
fi

if [ -n "$ADB_CMD" ]; then
    echo "Esperando dispositivo Android (emulador o físico)..."
    ADB_OK=0
    for i in {1..30}; do
        devices=$("$ADB_CMD" devices 2>/dev/null | grep -w "device" | awk '{print $1}')
        if [ -n "$devices" ]; then
            success=0
            for dev in $devices; do
                if "$ADB_CMD" -s "$dev" reverse tcp:8081 tcp:8081 2>/dev/null; then
                    echo "adb reverse configurado con éxito para el dispositivo: $dev"
                    success=1
                fi
            done
            if [ $success -eq 1 ]; then
                echo
                ADB_OK=1
                break
            fi
        fi
        sleep 2
    done
    if [ $ADB_OK -eq 0 ]; then
        echo "[AVISO] No se detectó ningún dispositivo o no se pudo configurar adb reverse."
        echo "Cuando el dispositivo esté conectado, ejecuta manualmente:"
        echo "  adb reverse tcp:8081 tcp:8081"
        echo
    fi
else
    echo "[AVISO] No se encontró adb."
    echo "Si usas un dispositivo, recuerda ejecutar manualmente:"
    echo "  adb reverse tcp:8081 tcp:8081"
    echo
fi

# --- 2. Cargar variables de entorno (si existe un archivo .env local, ignorado por git) ---
if [ -f ".env" ]; then
    export $(cat .env | xargs)
fi

# --- 3. Levantar el backend ---
echo "Levantando el backend..."
echo "Para frenarlo: Ctrl+C"
echo "======================================================="
echo

# Asegurar permisos de ejecución en mvnw
chmod +x ./mvnw

./mvnw spring-boot:run -Dmaven.test.skip=true
