from fastapi import FastAPI

app = FastAPI()
@app.get("/")
async def root():
    return {
        "service" : "verification_hub",
        "status" : "RUNNING",
    }
@app.get("/health")
async def health():
    return {"status" : "UP"}