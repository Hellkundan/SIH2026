# For linux

[for creating virtual environment]

`python3 -m venv .venv `

 [python virtual environment]

`source .venv/bin/activate`

[you should in src/verification_hub folder]

`pip install -r requirement.txt `

[run this command ]

`uvicorn app.main:app --reload --port 8000 `

[click on this url for launching it in browser]

`http://127.0.0.1:8000 `

[swagger api documentation]

`http://127.0.0.1:8000/docs `

# For windows

 [for creating virtual environment]

`python -m venv .venv`

 [python virtual environment for run on CMD] 

`.venv\Scripts\activate`

 [python virtual environment for run on powershell]

`.venv\Scripts\Activate.ps1`

 [you should in src/verification_hub folder]

`pip install -r requirements.txt`

 [run this command ]

`uvicorn app.main:app --reload --port 8000`

[click on this url for launching it in browser]

`http://127.0.0.1:8000 `

[swagger api documentation]

`http://127.0.0.1:8000/docs `


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

