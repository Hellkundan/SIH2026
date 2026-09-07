from fastapi import FastAPI


app = FastAPI()
@app.get("/")
async def root():
    return {
        "service" : "verification-hub",
        "status" : "RUNNING",
    }
@app.get("/health")
async def health():
    return {"status" : "UP"}