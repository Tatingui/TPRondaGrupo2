@echo off
echo Configurando git hooks...
git config core.hooksPath .githooks
if errorlevel 1 (
    echo ERROR: No se pudo configurar el hooksPath. Estas parado dentro del repo?
    pause
    exit /b 1
)
echo Hooks configurados correctamente.
echo Ahora antes de cada push se van a correr los tests automaticamente.
pause
