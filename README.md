# Start the DIXY Project Locally

This guide starts the complete DIXY platform:

- Frontend: Vite/TanStack Start on port `5173`
- Spring Boot backend on port `8080`
- Verification Hub on port `8000`
- OCR service on port `8001`
- AI service on port `8002`
- PostgreSQL on port `5432`

## Recommended: Docker Compose

### Prerequisites

Install:

- Docker Engine or Docker Desktop
- Docker Compose v2

From the repository root:

```bash
docker compose up -d --build
docker compose ps
```

Check the services:

```bash
curl http://localhost:8000/health
curl http://localhost:8001/health
curl http://localhost:8002/health
curl http://localhost:8080/actuator/health
```

Open the frontend separately:

```bash
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

Then open <http://localhost:5173>.

View logs:

```bash
docker compose logs -f backend
docker compose logs -f verification-hub
docker compose logs -f ocr
docker compose logs -f ai-service
```

Stop the complete stack:

```bash
docker compose down
```

Stop it and remove the PostgreSQL volume:

```bash
docker compose down -v
```

The second command deletes local database data.

## Windows setup

Open PowerShell in the repository root. Docker Compose commands are the same
on Windows:

```powershell
docker compose up -d --build
docker compose ps
```

Start the frontend in a second PowerShell window:

```powershell
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

Open <http://localhost:5173>.

### Native Windows startup

Install these prerequisites:

- Java 21
- Python 3.12 or newer
- Node.js and npm
- PostgreSQL 17 or newer
- Tesseract OCR, added to `PATH`

Create the Python environment from the repository root:

```powershell
py -3 -m venv .venv
.\.venv\Scripts\Activate.ps1
python -m pip install -r src\verification_hub\requirements.txt -r src\ocr\requirements.txt -r src\AI\requirements.txt networkx
```

Run each Python service in a separate PowerShell window:

```powershell
cd src\verification_hub
..\..\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
```

```powershell
cd src\ocr
..\..\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8001
```

```powershell
cd src
$env:PYTHONPATH = (Get-Location).Path
..\.venv\Scripts\python.exe -m uvicorn AI.dixy_api:app --host 0.0.0.0 --port 8002
```

Start the backend in another PowerShell window:

```powershell
.\gradlew.bat bootRun
```

Start the frontend in another PowerShell window:

```powershell
cd frontend
npm install
npm run dev -- --host 0.0.0.0
```

If PowerShell blocks virtual environment activation, run this once for the
current user and then activate the environment again:

```powershell
Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
```

For Tesseract, install the Windows package and ensure this command works:

```powershell
tesseract --version
```

## Native fallback

Use this when Docker is unavailable. PostgreSQL is still required for the
Spring Boot backend.

### 1. Start PostgreSQL

Create a database and user matching `src/backend/application.properties`:

```text
database: dixy
user: dixy
password: dixy_password
port: 5432
```

Or set these environment variables before starting the backend:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dixy
export SPRING_DATASOURCE_USERNAME=dixy
export SPRING_DATASOURCE_PASSWORD=dixy_password
```

### 2. Start the Python services

The project can be on a filesystem that does not support virtualenv
symlinks. In that case, place the environment under `/tmp`:

```bash
python3 -m venv --copies /tmp/sih2026-venv
/tmp/sih2026-venv/bin/pip install \
  -r src/verification_hub/requirements.txt \
  -r src/ocr/requirements.txt \
  -r src/AI/requirements.txt \
  networkx
```

Run each command in a separate terminal from the repository root:

```bash
cd src/verification_hub
/tmp/sih2026-venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000
```

```bash
cd src/ocr
/tmp/sih2026-venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8001
```

```bash
cd src
PYTHONPATH="$PWD" /tmp/sih2026-venv/bin/uvicorn AI.dixy_api:app --host 0.0.0.0 --port 8002
```

The OCR service also requires the system Tesseract binary:

```bash
sudo apt install tesseract-ocr
```

### 3. Start the backend

From the repository root:

```bash
./gradlew bootRun
```

The backend starts on <http://localhost:8080> after PostgreSQL is reachable.

### 4. Start the frontend

From `frontend/`:

```bash
npm install
npm run dev -- --host 0.0.0.0
```

Open <http://localhost:5173>.

If npm hangs or reports symlink errors, move or copy the repository to an
ext4-backed directory such as `~/projects/SIH2026`, then run the command again.

## Login and API usage

The backend login endpoint is:

```text
POST http://localhost:8080/auth/login
```

Use the returned token on protected endpoints:

```text
Authorization: Bearer <token>
```

API documentation is available at:

- Backend OpenAPI UI: <http://localhost:8080/swagger-ui/index.html>
- Verification Hub: <http://localhost:8000/docs>
- OCR: <http://localhost:8001/docs>
- AI: <http://localhost:8002/docs>

## Troubleshooting

- `docker: command not found`: install Docker, or use the native fallback.
- `Failed to determine a suitable driver class`: PostgreSQL is unavailable or
  the datasource variables are incorrect.
- `No module named uvicorn`: run the Python environment installation above and
  use `/tmp/sih2026-venv/bin/uvicorn`.
- `No module named networkx`: install `networkx`; it is required by the AI
  collusion graph module.
- Frontend npm install hangs on `/media/...`: use an ext4-backed project path.
- Port already in use: stop the existing process or change the corresponding
  port and service URL configuration.
