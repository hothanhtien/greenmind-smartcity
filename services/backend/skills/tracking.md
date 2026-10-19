# Skill: Tracking

Covers: Locations, Checkins, Todos, Daily Spending.

## Files
- `src/routes/locationRoutes.ts` → `src/controller/locationController.ts`
- `src/routes/checkinRoutes.ts` → `src/controller/checkinController.ts`
- `src/routes/todoRoutes.ts` → `src/controller/todoController.ts`
- `src/routes/dailySpendingRoutes.ts` → `src/controller/dailySpendingController.ts`
- Entities: `src/entity/locations.ts`, `src/entity/checkin.ts`, `src/entity/todos.ts`, `src/entity/daily_spend.ts`

---

## Routes — base: `/locations`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/locations/` | `jwt` | `createLocation()` |
| GET | `/locations/latest` | `jwt` | `GetLatestLocation()` |
| GET | `/locations/distanceToday` | `jwt` | `GetDistanceToday()` |
| GET | `/locations/` | `jwt` | `GetLocations()` |
| GET | `/locations/:id` | `jwt` | `getLocationById()` |
| PUT | `/locations/:id` | `jwt` | `updateLocationById()` |
| DELETE | `/locations/:id` | `jwt` | `deleteLocationById()` |

---

## Routes — base: `/checkins`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/checkins/` | `jwt` | `CreateCheckin()` |
| GET | `/checkins/` | `jwt` | `GetCheckins()` |
| PUT | `/checkins/:id` | `jwt` | `UpdateCheckin()` |
| DELETE | `/checkins/:id` | `jwt` | `DeleteCheckin()` |
| GET | `/checkins/get-checkins-by-period` | `jwt` | `GetCheckinsByPeriod()` |
| GET | `/checkins/get-checkins-by-params` | `jwt` | `GetCheckinsByParams()` |

---

## Routes — base: `/todos`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/todos/` | `jwt` | `createTodo()` |
| POST | `/todos/batch` | `jwt` | `createTodosList()` |
| GET | `/todos/` | `jwt` | `getTodos()` |
| GET | `/todos/:id` | `jwt` | `getTodoById()` |
| PUT | `/todos/:id` | `jwt` | `updateTodo()` |
| PATCH | `/todos/:id/toggle` | `jwt` | `toggleTodo()` |
| DELETE | `/todos/:id` | `jwt` | `deleteTodo()` |

---

## Routes — base: `/daily-spending`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| GET | `/daily-spending/` | `jwt` | `getAverageDailySpend()` |
| POST | `/daily-spending/` | `jwt` | `CreateOrUpdateSpend()` |
| POST | `/daily-spending/average-daily` | `jwt` | `createOrUpdateAverageDaily()` |

---

## Related Skills
- [metrics.md](./metrics.md) — metric controllers compute aggregates from spending & location data
