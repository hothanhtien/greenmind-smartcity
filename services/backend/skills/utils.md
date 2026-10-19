# Skill: Utilities

## Files
- `src/utils/jwtHelper.ts`
- `src/utils/bitmapHelper.ts`
- `src/utils/googleLoginHelper.ts`
- `src/utils/usernameHelper.ts`
- `src/utils/modelMatcher.ts`
- `src/utils/verifySurveyHelper.ts`

---

## `class JWTHelper` — `jwtHelper.ts`

| Method | Calls | Called By |
|---|---|---|
| `createAccessToken(payload)` | `jwt.sign()` | `userController` (login), `tokenController` |
| `createRefreshToken(payload)` | `jwt.sign()` | `userController` (login) |
| `createTokenPair(payload)` | `createAccessToken()`, `createRefreshToken()` | `userController` |
| `verifyAccessToken(token)` | `jwt.verify()` | `jwtAuthMiddleware` |
| `verifyRefreshToken(token)` | `jwt.verify()` | `tokenController` |

Interfaces: `JWTPayload { userId, role?, email? }`, `TokenPair { accessToken, refreshToken }`

---

## `class BitmapHelper` — `bitmapHelper.ts`

Uses Redis bitmaps to track token blacklisting.

| Method | Calls | Called By |
|---|---|---|
| `allocateID()` | `redis.bitpos()`, `redis.setbit()` | — |
| `releaseID(id)` | `redis.setbit()` | — |
| `blacklistID(id)` | `redis.setbit()` | `blacklistToken()` |
| `blacklistIDWithTTL(id, ttlMs)` | `redis.setbit()`, `redis.set(...PX)` | `blacklistToken()` |
| `isBlacklisted(id)` | `redis.getbit()` | `isTokenBlacklisted()` |
| `generateTokenId(token)` | pure hash | `blacklistToken()`, `isTokenBlacklisted()` |
| `blacklistToken(token, ttlMs?)` | `generateTokenId()`, `blacklistIDWithTTL()` / `blacklistID()` | `Logout()`, `revokeTokenMiddleware` |
| `isTokenBlacklisted(token)` | `generateTokenId()`, `isBlacklisted()` | `jwtAuthMiddleware` |
| `cleanupBitmap(maxId)` | `redis.keys()`, `redis.getbit()`, `redis.setbit()` | maintenance |

---

## Other Utilities

### `googleLoginHelper.ts`
- Google OAuth token exchange / user info fetch
- called by: `userController.LoginWithGoogle()`

### `usernameHelper.ts`
- Auto-generate username from email or name
- called by: `userController.RegisterWithEmail()`

### `modelMatcher.ts`
- Matches user profile to behavior model
- called by: `modelController`

### `verifySurveyHelper.ts`
- Core logic for survey verification scoring
- called by: `surveyVerifyController.verifySurvey()`
