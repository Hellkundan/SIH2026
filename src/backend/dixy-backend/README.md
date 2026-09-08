# DIXY Backend — AI-Powered Bid Compliance Platform
## Member 6: Security, DevOps & QA

---

### Overview
This is the Spring Boot 3 / Java 17 backend service for DIXY, providing JWT authentication, RBAC authorization, tamper-resistant audit logging, and document compliance endpoints.

---

### Prerequisites
- **Java 17** (Eclipse Temurin / OpenJDK)
- **Maven 3.8+**
- **Docker & Docker Compose** (for containerized deployment)
- **PostgreSQL 16** (if running locally without Docker)

---

### Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```

Key variables configured in `.env`:
| Variable | Description | Default / Example |
| :--- | :--- | :--- |
| `DB_HOST` | Database host name (`localhost` for host, `postgres` in Docker) | `localhost` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `dixy_db` |
| `DB_USERNAME` | Database username | `dixy_user` |
| `DB_PASSWORD` | Database password | `dixy_dev_password` |
| `JWT_SECRET` | 256-bit secret key (min 32 bytes) | *(Set via .env)* |
| `SERVER_PORT` | HTTP port | `8080` |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `http://localhost:3000,http://localhost:5173` |

---

### Option A: Docker Deployment (Recommended)

1. Build and start the entire multi-container stack:
```bash
docker compose up --build -d
```

2. Check service status and health:
```bash
docker compose ps
docker compose logs -f
```

3. Application will be available at:
- **API URL**: `http://localhost:8080`
- **Health Check**: `http://localhost:8080/api/v1/public/health`
- **Swagger Docs**: `http://localhost:8080/swagger-ui.html`

4. Stop the stack:
```bash
docker compose down
```

---

### Option B: Local Development (IntelliJ / Maven)

1. Start only PostgreSQL via Docker:
```bash
docker compose up -d postgres
```

2. Run the Spring Boot application locally:
```bash
mvn spring-boot:run
```

3. Run automated tests:
```bash
mvn test
```

---

### Troubleshooting Docker

1. **Port conflicts**: If port 5432 or 8080 is already occupied on your host machine, modify `DB_PORT` or `SERVER_PORT` in `.env`.
2. **Database connection failed on startup**: Ensure the Docker healthcheck is active; the `backend` service is configured with `depends_on: postgres: condition: service_healthy`.
3. **Data persistence**: PostgreSQL storage is maintained across restarts using the named volume `postgres_data`. To reset database state, run `docker compose down -v`.
