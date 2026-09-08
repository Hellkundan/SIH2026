# DIXY DevOps and Deployment

## Services

| Service | Local port | Container port | Health endpoint |
|---|---:|---:|---|
| Spring Boot backend | 8080 | 8080 | `/actuator/health` when actuator is enabled |
| Verification Hub | 8000 | 8000 | `/health` |
| OCR service | 8001 | 8001 | `/health` |

The backend remains in-memory by design. Restarting its container clears tender,
bidder, document, bid, requirement, and user data. No database is required for
this deployment.

## Local Docker deployment

Prerequisites: Docker Desktop with Compose v2.

```powershell
docker compose build
docker compose up -d
docker compose ps
```

Useful checks:

```powershell
Invoke-WebRequest http://localhost:8000/health
Invoke-WebRequest http://localhost:8001/health
docker compose logs -f backend
```

Stop the stack with:

```powershell
docker compose down
```

## Backend authentication

Obtain a development token from `POST http://localhost:8080/auth/login`, then
send it as `Authorization: Bearer <token>`. The seeded development accounts
are documented in the Step 4 implementation and must be replaced through
environment-backed secrets before any shared deployment.

## Configuration

Set these backend environment variables in a deployment secret store:

- `APP_JWT_SECRET`: base64-encoded signing key
- `APP_JWT_EXPIRATION_MS`: token lifetime in milliseconds

Do not commit production credentials, JWT keys, or uploaded documents.

## CI/CD flow

The workflow in `.github/workflows/ci.yml` runs on pushes and pull requests:

1. Builds and tests the Gradle backend on Java 21.
2. Compiles the Verification Hub Python sources.
3. Compiles the OCR Python sources and installs its runtime dependencies.

A production pipeline should build and push the three images after CI passes,
then deploy them using the same Compose service boundaries or an equivalent
container platform. Keep image tags tied to the Git commit SHA for rollback.

## Operational notes

- Put a reverse proxy or managed ingress in front of the services in production.
- Restrict CORS and expose only the backend publicly; keep OCR and Verification
  Hub on a private network where possible.
- Add centralized logs and request IDs before multi-instance deployment.
- Add persistent storage only through a separately approved architecture change;
  this project intentionally does not configure one.