# Skill: Middlewares

## Files
- `src/middlewares/jwtMiddleware.ts`
- `src/middlewares/adminMiddleware.ts`
- `src/middlewares/corsMiddleware.ts`
- `src/middlewares/loggingMiddleware.ts`

---

## Functions

### `jwtAuthMiddleware(req, res, next)` — `jwtMiddleware.ts`
- Extracts token from `req.cookies.access_token` or `Authorization: Bearer <token>` header
- calls: `JWTHelper.verifyAccessToken(token)` → `BitmapHelper.isTokenBlacklisted(token)`
- sets: `req.user = { userId, role }`
- used by: **all protected routes**

### `revokeTokenMiddleware(req, res, next)` — `jwtMiddleware.ts`
- calls: `BitmapHelper.blacklistToken(token, 7d)`
- used by: logout flow

### `adminMiddleware(req, res, next)` — `adminMiddleware.ts`
- requires: `req.user.role === 'admin'`
- used by: admin-only routes

### `staffOrAdminMiddleware(req, res, next)` — `adminMiddleware.ts`
- requires: `req.user.role === 'admin' || 'expert'`
- used by: staff-level routes

### `corsMiddleware(req, res, next)` — `corsMiddleware.ts`
- calls: `cors(corsOptions)`, `logger.debug()` for OPTIONS
- used by: production `startServer()`

### `devCorsMiddleware` — `corsMiddleware.ts`
- permissive CORS (all origins, credentials: true)
- used by: development `startServer()`

### `loggingMiddleware(req, res, next)` — `loggingMiddleware.ts`
- calls: `getLogger()`, `logger.logHTTPRequest()`
- patches `res.end` to capture response duration

---

## Call Chain (auth flow)
```
Request
  → jwtAuthMiddleware
      → JWTHelper.verifyAccessToken()   [utils/jwtHelper.ts]
      → BitmapHelper.isTokenBlacklisted()  [utils/bitmapHelper.ts → redis]
      → req.user = { userId, role }
  → adminMiddleware / staffOrAdminMiddleware  (if applicable)
  → controller handler
```

→ See [utils.md](./utils.md) for `JWTHelper` and `BitmapHelper` internals.
