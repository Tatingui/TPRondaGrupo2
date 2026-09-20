#!/bin/bash

echo "======================================================="
echo "  Ronda Backend - Arranque rapido (macOS / Linux)"
echo "======================================================="
echo

# Ir al directorio del script
cd "$(dirname "$0")"

# --- 1. Configurar ADB reverse ---
ADB_CMD=""

if command -v adb &> /dev/null; then
    ADB_CMD="adb"
elif [ -f "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
    ADB_CMD="$HOME/Library/Android/sdk/platform-tools/adb"
elif [ -f "$HOME/Android/Sdk/platform-tools/adb" ]; then
    ADB_CMD="$HOME/Android/Sdk/platform-tools/adb"
fi

if [ -n "$ADB_CMD" ]; then
    echo "Esperando al emulador de Android..."
    ADB_OK=0
    for i in {1..30}; do
        if "$ADB_CMD" devices 2>/dev/null | grep -q "emulator.*device"; then
            if "$ADB_CMD" reverse tcp:8081 tcp:8081 2>/dev/null; then
                echo "adb reverse configurado con exito. OK."
                echo
                ADB_OK=1
                break
            fi
        fi
        sleep 2
    done
    if [ $ADB_OK -eq 0 ]; then
        echo "[AVISO] No se detecto el emulador o no se pudo configurar adb reverse."
        echo "Cuando el emulador este corriendo, ejecuta manualmente:"
        echo "  adb reverse tcp:8081 tcp:8081"
        echo
    fi
else
    echo "[AVISO] No se encontro adb."
    echo "Si usas emulador, recuerda ejecutar manualmente:"
    echo "  adb reverse tcp:8081 tcp:8081"
    echo
fi

# --- 2. Credenciales de email (cuenta compartida del equipo) ---
export MAIL_USERNAME="rondauade@gmail.com"
export MAIL_PASSWORD="ukpblecflriqfyyd"

# --- 3. Levantar el backend ---
echo "Levantando el backend..."
echo "Para frenarlo: Ctrl+C"
echo "======================================================="
echo

# Asegurar permisos de ejecución en mvnw
chmod +x ./mvnw

./mvnw spring-boot:run -Dmaven.test.skip=true
