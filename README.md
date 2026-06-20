# Trabalho Final Web3

## Visão Geral da Arquitetura

Este projeto é composto por três serviços independentes:

1. frontend
   - Aplicação Node.js/Express que serve páginas HTML estáticas.
   - Realiza chamadas ao backend para gerar e verificar códigos de login.
   - Usa `sessionStorage` para armazenar o token JWT.

2. user_service
   - Serviço Spring Boot em user_service.
   - CRUD de usuários, autenticação com JWT e endpoints protegidos.
   - Publica mensagens para RabbitMQ quando um email deve ser enviado.
   - Expõe endpoints:
     - `/auth/request-code`
     - `/auth/verify-code`
     - `/users/me`
     - `/users/test/customer`
     - `/users/test/administrator`

3. email_service
   - Serviço Spring Boot em email_service.
   - Consome mensagens RabbitMQ e envia email via SMTP Gmail.
   - Usa a fila `default.email`.

Comunicação entre serviços:
- frontend -> user_service via HTTP
- user_service -> RabbitMQ via `spring.rabbitmq.addresses`
- email_service -> RabbitMQ e Gmail SMTP

## Pré-requisitos

- JDK 17
- Maven (ou usar `./mvnw` / `mvnw.cmd`)
- Node.js + npm
- MySQL
- Conta CloudAMQP / RabbitMQ
- Conta Gmail para SMTP

## Instruções de Configuração

### MySQL

Crie os bancos:

```sql
CREATE DATABASE ms_user;
CREATE DATABASE ms_email;
```

Ajuste o usuário/senha se necessário em application.properties.

### CloudAMQP

- Crie uma conta no CloudAMQP.
- Crie uma instância RabbitMQ.
- Copie a URL AMQPS e configure em:
  - application.properties
  - application.properties

### Gmail SMTP

Configure em application.properties:

```properties
spring.mail.username=seu-email@gmail.com
spring.mail.password=sua-senha-ou-app-password
```

> Recomenda-se usar senha de app do Gmail se 2FA estiver ativado.

### Variáveis de ambiente / configs

Ajuste em:
- application.properties
- application.properties

Parâmetros importantes:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.rabbitmq.addresses`
- `spring.mail.username`
- `spring.mail.password`

## Como Executar

### 1. Frontend

```bash
cd frontend
npm install
node server.js
```

Acessar: `http://localhost:3000`

### 2. User Service

```bash
cd user_service
./mvnw spring-boot:run
```

A aplicação roda em: `http://localhost:8081`

### 3. Email Service

```bash
cd email_service
./mvnw spring-boot:run
```

A aplicação roda em: `http://localhost:8082`

## Fluxo Completo

1. Usuário acessa `http://localhost:3000`
2. Envia email para `/auth/request-code`
3. Recebe código e verifica em `/auth/verify-code`
4. user_service gera token JWT
5. frontend armazena token em `sessionStorage`
6. frontend faz chamadas protegidas para `/users/me` e `/users/test/customer`
7. user_service publica mensagem na fila RabbitMQ
8. email_service consome a fila e envia email via Gmail SMTP

## Endpoints Principais

- `POST http://localhost:8081/auth/request-code`
- `POST http://localhost:8081/auth/verify-code`
- `GET http://localhost:8081/users/me`
- `GET http://localhost:8081/users/test/customer`
- `GET http://localhost:3000/dashboard`

## Scripts de Inicialização

### iniciar.ps1 (Windows)

```powershell
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\frontend'; npm install; node server.js"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\user_service'; .\mvnw spring-boot:run"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$root\email_service'; .\mvnw spring-boot:run"
```

### start.sh (Linux)

```bash
#!/usr/bin/env bash
ROOT="$(cd "$(dirname "$0")" && pwd)"

if command -v gnome-terminal >/dev/null 2>&1; then
  gnome-terminal -- bash -lc "cd \"$ROOT/frontend\" && npm install && node server.js; exec bash"
  gnome-terminal -- bash -lc "cd \"$ROOT/user_service\" && ./mvnw spring-boot:run; exec bash"
  gnome-terminal -- bash -lc "cd \"$ROOT/email_service\" && ./mvnw spring-boot:run; exec bash"
else
  echo "gnome-terminal não encontrado. Execute manualmente ou adapte para seu terminal."
fi
```

> Para Linux: `chmod +x start.sh`