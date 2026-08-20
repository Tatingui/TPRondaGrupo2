#!/bin/bash
echo "Configurando git hooks..."

if ! git rev-parse --git-dir >/dev/null 2>&1; then
    echo "ERROR: No estas parado dentro de un repositorio git."
    exit 1
fi

git config core.hooksPath .githooks
chmod +x .githooks/*

echo "Hooks configurados correctamente."
echo "Ahora antes de cada push se van a correr los tests automaticamente."
