# GreenMind

AI-powered smart city platform for waste classification, pollution impact estimation and
community-driven waste management.

> **About this repository.** It contains the application logic and the API contracts of GreenMind.
> The project's **core know-how is private and not included**: trained model weights, waste →
> pollutant coefficients, Green Score weights, mass-estimation calibration, landfill scoring
> parameters, LLM prompts, business rules and the OCEAN/behavior engine. Each service ships a
> `core.example/` with the same schema and **placeholder values**, so everything starts, but results
> are not meaningful without the real core.

## Structure

| Path | What | Stack |
|---|---|---|
| `services/ai` | AI service: waste segmentation, pollutant impact, mass estimation, landfill scoring, invoice OCR | Python, FastAPI, YOLOv8, MiDaS, RapidOCR |
| `services/backend` | Main API: auth, households, waste reports, campaigns, payments, chat | Node.js, TypeScript, Express, TypeORM |
| `services/web` | Admin / community dashboard | Next.js |
| `apps/mobile` | Mobile app (Android, iOS) with a desktop build for development | Kotlin Multiplatform, Compose |
| `infra` | Local PostgreSQL + Redis | Docker Compose |
| `docs/api` | HTTP contracts (OpenAPI) | |

## Run locally

Prerequisites: Docker, Node.js 22, Python 3.11+, JDK 17+.

```bash
# 1. Infrastructure
cp infra/.env.example infra/.env            # set POSTGRES_PASSWORD / REDIS_PASSWORD
docker compose -f infra/docker-compose.yml --env-file infra/.env up -d

# 2. AI service  (http://localhost:8000/docs)
cd services/ai
python -m venv .venv && .venv/Scripts/activate   # macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env                         # API keys; GREENMIND_CORE_DIR / GREENMIND_MODEL_DIR if you have the core
uvicorn app.main:app --port 8000

# 3. Backend  (http://localhost:4000)
cd services/backend
yarn install
cp .env.example .env                         # DB/Redis from step 1, AI_SERVICE_URL=http://localhost:8000
npx tsx scripts/dev-db-bootstrap.ts          # first run on an empty database
yarn dev

# 4. Web  (http://localhost:3000)
cd services/web
npm install --legacy-peer-deps
cp .env.example .env.local
npm run dev

# 5. Mobile, desktop build
cd apps/mobile
GREENMIND_API_URL=http://localhost:4000 GREENMIND_AI_URL=http://localhost:8000 ./gradlew :composeApp:run
```

## Private core

| Variable | Service | Points to |
|---|---|---|
| `GREENMIND_CORE_DIR` | `services/ai` | `greenmind-core/ai` — JSON coefficients, prompts, OCEAN plugins |
| `GREENMIND_MODEL_DIR` | `services/ai` | `greenmind-core/models` — trained `.pt` weights |
| `GREENMIND_CORE_DIR` | `services/backend` | `greenmind-core/backend` — behavior parameters, business rules |
| `NEXT_PUBLIC_CAMPAIGN_POLLUTION_THRESHOLD` | `services/web` | campaign suggestion threshold |

Never commit `.env*` files, the core directory or model weights; `.gitignore` blocks them.
