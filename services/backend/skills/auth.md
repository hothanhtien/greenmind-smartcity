# Skill: Auth (User Authentication)

## Files
- `src/routes/userRoutes.ts`
- `src/controller/userController.ts`
- Entity: `src/entity/user.ts`

---

## Routes — base: `/auth`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/auth/login/email` | — | `LoginWithEmail()` |
| POST | `/auth/register/email` | — | `RegisterWithEmail()` |
| POST | `/auth/login/google` | — | `LoginWithGoogle()` |
| GET | `/auth/profile` | `jwtAuthMiddleware` | `GetProfile()` |
| POST | `/auth/logout` | `jwtAuthMiddleware` | `Logout()` |
| GET | `/auth/get-alls` | `jwtAuthMiddleware`, `adminMiddleware` | `GetAllUsers()` |

---

## Controller Functions — `src/controller/userController.ts`

- `LoginWithEmail(req, res)`
  - calls: DB lookup by email, `bcrypt.compare()`, `JWTHelper.createTokenPair()`
  - returns: `{ accessToken, refreshToken }`

- `RegisterWithEmail(req, res)`
  - calls: `usernameHelper`, DB insert, `bcrypt.hash()`, `JWTHelper.createTokenPair()`

- `LoginWithGoogle(req, res)`
  - calls: `googleLoginHelper` (exchange Google token → user info), DB upsert, `JWTHelper.createTokenPair()`

- `GetProfile(req, res)`
  - calls: DB lookup by `req.user.userId`

- `Logout(req, res)`
  - calls: `BitmapHelper.blacklistToken(token, 7d)`

- `GetAllUsers(req, res)`
  - calls: DB query all users (admin only)

---

## Related Skills
- [middlewares.md](./middlewares.md) — `jwtAuthMiddleware`, `adminMiddleware`
- [utils.md](./utils.md) — `JWTHelper`, `BitmapHelper`, `googleLoginHelper`, `usernameHelper`
- [tokens.md](./tokens.md) — refresh token flow
