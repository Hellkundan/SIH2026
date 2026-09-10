from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    APP_NAME: str = "Verification Hub"
    APP_VERSION: str = "1.0.0"
    PROVIDER_TIMEOUT_SECONDS: float = 5.0
    RETRY_ATTEMPTS: int = 2
    RETRY_BACKOFF_SECONDS: float = 0.25
    PROVIDER_MODE: str = "mock"  # mock | real (real adapters can be added later)
    EVIDENCE_DIR: str = "verification"

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")


settings = Settings()
