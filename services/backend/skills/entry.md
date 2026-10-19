# Entry Map — Green MindMap Backend

> Read this file first. Use it to identify which skill file to open next.

## Entry Point
- **File:** `src/index.ts`
- **Function:** `startServer()` — initializes infrastructure, middleware, and all routers.

---

## Skill Index

| Skill File | Covers |
|---|---|
| [infrastructure.md](./infrastructure.md) | DB (TypeORM/Postgres), Redis cache, Logger, `initInfrastructure()`, `config/env` |
| [middlewares.md](./middlewares.md) | `jwtAuthMiddleware`, `adminMiddleware`, `staffOrAdminMiddleware`, `corsMiddleware`, `loggingMiddleware` |
| [utils.md](./utils.md) | `JWTHelper`, `BitmapHelper`, `googleLoginHelper`, `usernameHelper`, `modelMatcher`, `verifySurveyHelper` |
| [auth.md](./auth.md) | `/auth` routes — login (email/google), register, profile, logout, get-all-users |
| [tokens.md](./tokens.md) | `/tokens` routes — refresh access token |
| [questions.md](./questions.md) | `/questions` and `/question-sets` CRUD routes |
| [templates.md](./templates.md) | `/templates` and `/traits` CRUD routes |
| [assessments.md](./assessments.md) | `/big-five`, `/behaviors`, `/behavior-feedbacks`, `/user-answers` routes |
| [survey.md](./survey.md) | `/scenarios-survey`, `/pre-app-survey`, survey-verify logic |
| [models.md](./models.md) | `/models` routes — behavior model creation, feedbacks |
| [tracking.md](./tracking.md) | `/locations`, `/checkins`, `/todos`, `/daily-spending` routes |
| [metrics.md](./metrics.md) | `/metrics` routes — 8 metric controllers (avg spend, variability, brand novelty, etc.) |
| [media.md](./media.md) | `/ocr`, `/healthy-food-ratio`, `/brands` — file upload routes |

---

## Quick Route → Skill Lookup

| Base Path | Skill |
|---|---|
| `/auth` | auth.md |
| `/tokens` | tokens.md |
| `/locations` | tracking.md |
| `/questions`, `/question-sets` | questions.md |
| `/templates`, `/traits` | templates.md |
| `/big-five`, `/behaviors`, `/behavior-feedbacks`, `/user-answers` | assessments.md |
| `/scenarios-survey`, `/pre-app-survey` | survey.md |
| `/models` | models.md |
| `/daily-spending`, `/checkins`, `/todos` | tracking.md |
| `/metrics` | metrics.md |
| `/brands`, `/ocr`, `/healthy-food-ratio` | media.md |

---

## Middleware Quick Reference

All protected routes use `jwtAuthMiddleware`.
Admin-only routes also use `adminMiddleware`.
Staff/admin routes also use `staffOrAdminMiddleware`.

→ See [middlewares.md](./middlewares.md) for call chains.
→ See [utils.md](./utils.md) for `JWTHelper` / `BitmapHelper` internals.
