# Skill: Infrastructure & Config

## Files
- `src/index.ts` — app entry
- `src/config/env.ts` — all env config
- `src/infrastructure/index.ts` — `initInfrastructure()`
- `src/infrastructure/database.ts` — TypeORM `AppDataSource`
- `src/infrastructure/cache.ts` — ioredis `redis` instance
- `src/infrastructure/logger.ts` — `logger`, `LoggerClient`, `getLogger()`

---

## Functions

### `startServer()` — `src/index.ts`
- calls: `initInfrastructure()`, `devCorsMiddleware` / `corsMiddleware`, `express()`, `app.use(routes)`
- called by: process start

### `initInfrastructure()` — `src/infrastructure/index.ts`
- calls: `AppDataSource.initialize()`, `new Infrastructure({ database, cache, logger })`
- called by: `startServer()`

### `class Infrastructure` — `src/infrastructure/index.ts`
- properties: `database: DataSource`, `cache: Redis`, `logger: LoggerClient`

### `AppDataSource` — `src/infrastructure/database.ts`
- TypeORM DataSource (postgres)
- entities: `src/entity/**/*.ts`
- migrations: `src/migrations/*.ts`
- called by: `initInfrastructure()`, migration scripts

### `redis` — `src/infrastructure/cache.ts`
- ioredis instance
- called by: `BitmapHelper`, `initInfrastructure()`

### `logger` / `getLogger()` — `src/infrastructure/logger.ts`
- exports: `logger` (singleton), `getLogger()`, `LoggerClient` class
- key method: `logHTTPRequest(method, path, userId, statusCode, duration)`
- called by: `loggingMiddleware`, `corsMiddleware`, `BitmapHelper`, everywhere

---

## `config` object — `src/config/env.ts`
| Key | Usage |
|---|---|
| `config.app.port` | server listen port |
| `config.app.host` | server host |
| `config.app.env` | `'development'` / `'production'` |
| `config.jwt.secretKey` | JWT signing key |
| `config.jwt.algorithm` | JWT algorithm |
| `config.jwt.expire` | token expiry |
| `config.db.*` | postgres connection |
