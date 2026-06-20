#!/usr/bin/env bash
ROOT="$(cd "$(dirname "$0")" && pwd)"

if command -v gnome-terminal >/dev/null 2>&1; then
  gnome-terminal -- bash -lc "cd \"$ROOT/frontend\" && npm install && node server.js; exec bash"
  gnome-terminal -- bash -lc "cd \"$ROOT/user_service\" && ./mvnw spring-boot:run; exec bash"
  gnome-terminal -- bash -lc "cd \"$ROOT/email_service\" && ./mvnw spring-boot:run; exec bash"
else
  echo "gnome-terminal não encontrado. Execute manualmente ou adapte para seu terminal."
fi