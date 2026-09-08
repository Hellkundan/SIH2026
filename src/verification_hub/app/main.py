from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.verification_hub.app.api.routes import router
from src.verification_hub.appcore.config import settings

app = FastAPI(
    title=settings.APP_NAME,
    version=settings.APP_VERSION,
    description="verification hub",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)

@app.get("/")
async def root():
    return {
        "service": settings.APP_NAME,
        "status": "RUNNING",
    }

@app.get("/health")
async def health():
    return {"status": "UP"}