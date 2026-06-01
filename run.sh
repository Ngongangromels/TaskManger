#!/usr/bin/env bash
# ==========================================================
# Lancement de l'application STRMS sous Linux / macOS.
# Utilise le Maven Wrapper embarque (mvnw) : pas besoin
# d'installer Maven manuellement.
# Pre-requis : JDK 17+ installe.
# ==========================================================

set -e
cd "$(dirname "$0")"

echo "====================================================="
echo "  STRMS - Smart Task and Resource Management System"
echo "====================================================="
echo

if ! command -v java >/dev/null 2>&1; then
    echo "[ERREUR] Java n'est pas detecte dans le PATH."
    echo "Installez un JDK 17+ :"
    echo "  - Eclipse Adoptium : https://adoptium.net/"
    echo "  - Liberica         : https://bell-sw.com/pages/downloads/"
    exit 1
fi

# S'assurer que le wrapper est executable
chmod +x ./mvnw 2>/dev/null || true

./mvnw javafx:run
