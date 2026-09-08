from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    APP_NAME: str = "DIXY Verification Hub"
    APP_VERSION: str = "1.0.0"
    PROVIDER_TIMEOUT_SECONDS: float = 5.0
    RETRY_ATTEMPTS: int = 2

    model_config = SettingsConfigDict(env_file=".env", extra="ignore")

settings = Settings()