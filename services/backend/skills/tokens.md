# Skill: Tokens

## Files
- `src/routes/tokenRoutes.ts`
- `src/controller/tokenController.ts`
- Entity: `src/entity/token.ts`

---

## Routes — base: `/tokens`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| GET | `/tokens/token/access-token` | `jwtAuthMiddleware` | `GetNewToken()` |

---

## Controller Functions — `src/controller/tokenController.ts`

- `GetNewToken(req, res)`
  - calls: `JWTHelper.verifyRefreshToken()`, `JWTHelper.createAccessToken()`
  - returns new access token using the refresh token

---

## Related Skills
- [auth.md](./auth.md) — initial token pair creation at login
- [utils.md](./utils.md) — `JWTHelper`
