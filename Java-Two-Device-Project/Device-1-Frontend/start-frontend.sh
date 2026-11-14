#!/usr/bin/env bash
# ==============================================================================
# DREMLDS — DEVICE 1 (FRONTEND) STARTUP SCRIPT (LINUX / MACOS)
# Serves the static presentation UI on port 3000 using Python or Node.js
# ==============================================================================

PORT=${1:-3000}
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
cd "$DIR" || exit 1

echo "======================================================================"
echo " DREMLDS — Starting Device 1 Frontend Command Center"
echo "======================================================================"

if [ -f "config/config.js" ]; then
    BACKEND_URL=$(grep 'API_BASE_URL:' config/config.js | head -1 | awk -F '"' '{print $2}')
    echo " Configured Device 2 Backend: $BACKEND_URL"
fi

echo " Starting HTTP static web server on port $PORT..."
echo " Open your browser to: http://localhost:$PORT/src/index.html"
echo " Press CTRL+C to stop the server."
echo ""

if command -v python3 >/dev/null 2>&1; then
    python3 -m http.server "$PORT"
elif command -v python >/dev/null 2>&1; then
    python -m http.server "$PORT"
elif command -v node >/dev/null 2>&1; then
    node server.js "$PORT"
else
    echo "Error: Neither python3 nor node found. Please install Python or Node to serve files."
    exit 1
fi

