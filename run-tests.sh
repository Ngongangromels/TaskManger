#!/usr/bin/env bash
# Lance la suite de tests JUnit du projet STRMS via le Maven Wrapper.
# Pre-requis : JDK 17+ installe.

set -e
cd "$(dirname "$0")"

echo "====================================================="
echo "  STRMS - Exécution des tests JUnit"
echo "====================================================="
echo

chmod +x ./mvnw 2>/dev/null || true
./mvnw test
