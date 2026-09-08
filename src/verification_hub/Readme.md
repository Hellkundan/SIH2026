# For linux

`python3 -m venv .venv [for creating virtual environment]
source .venv/bin/activate [python virtual environment]
pip install -r requirement.txt [you should in src/verification_hub folder]
uvicorn app.main:app --reload --port 8000 [run this command ]
http://127.0.0.1:8000 [click on this url for launching it in browser]
http://127.0.0.1:8000/docs [swagger api documentation]`

# For windows

`python -m venv .venv [for creating virtual environment]
.venv\Scripts\activate [python virtual environment for run on CMD] 
.venv\Scripts\Activate.ps1 [python virtual environment for run on powershell]
pip install -r requirements.txt [you should in src/verification_hub folder]
uvicorn app.main:app --reload --port 8000 [run this command ]
http://127.0.0.1:8000 [click on this url for launching it in browser]
http://127.0.0.1:8000/docs [swagger api documentation]`


src/verfication_hub/data/mock -> store mock data in JSON format

# End Point for java backend:

Base URL:
http://127.0.0.1:8000

Verify one:
POST /api/v1/verification/verify

Verify all:
POST /api/v1/verification/verify-all

Available providers:
GET /api/v1/providers

Health:
GET /health

