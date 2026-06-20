$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm install; node server.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\user_service'; .\mvnw spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\email_service'; .\mvnw spring-boot:run"