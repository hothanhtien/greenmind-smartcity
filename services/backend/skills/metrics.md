# Skill: Metrics

All metric routes require `jwtAuthMiddleware`.

## Files
- `src/routes/metricsRoutes.ts`
- `src/controller/metrics/averageDailySpendController.ts`
- `src/controller/metrics/spendVariabilityController.ts`
- `src/controller/metrics/brandNoveltyController.ts`
- `src/controller/metrics/listAdherenceController.ts`
- `src/controller/metrics/dailyDistanceKmController.ts`
- `src/controller/metrics/novelLocationRatioController.ts`
- `src/controller/metrics/publicTransitRatioController.ts`
- `src/controller/metrics/nightOutFreqController.ts`
- Entity: `src/entity/metrics.ts`, `src/entity/night_out_freq.ts`

---

## Routes — base: `/metrics`

| Method | Path | Controller File | Handler |
|---|---|---|---|
| GET | `/metrics/avg-daily-spend` | `averageDailySpendController` | `getAvgDailySpend()` |
| POST | `/metrics/avg-daily-spend` | `averageDailySpendController` | `updateAvgSpend()` |
| GET | `/metrics/spend-variability` | `spendVariabilityController` | `getSpendVariability()` |
| POST | `/metrics/spend-variability` | `spendVariabilityController` | `updateSpendVariability()` |
| GET | `/metrics/brand-novelty` | `brandNoveltyController` | `getBrandNovelty()` |
| POST | `/metrics/brand-novelty` | `brandNoveltyController` | `updateBrandNovelty()` |
| GET | `/metrics/list-adherence` | `listAdherenceController` | `getListAdherence()` |
| POST | `/metrics/list-adherence` | `listAdherenceController` | `updateListAdherence()` |
| GET | `/metrics/daily-distance-km` | `dailyDistanceKmController` | `getDailyDistanceKm()` |
| POST | `/metrics/daily-distance-km` | `dailyDistanceKmController` | `updateDailyDistanceKm()` |
| GET | `/metrics/novel-location-ratio` | `novelLocationRatioController` | `getNovelLocationRatio()` |
| POST | `/metrics/novel-location-ratio` | `novelLocationRatioController` | `updateNovelLocationRatio()` |
| GET | `/metrics/public-transit-ratio` | `publicTransitRatioController` | `getPublicTransitRatio()` |
| POST | `/metrics/public-transit-ratio` | `publicTransitRatioController` | `updatePublicTransitRatio()` |
| GET | `/metrics/night-out-freq` | `nightOutFreqController` | `getNightOutFreq()` |
| POST | `/metrics/night-out-freq` | `nightOutFreqController` | `countNightOut()` |

---

## Pattern per metric controller
Each controller exports two functions:
- `get<MetricName>(req, res)` — DB fetch current metric value for `req.user.userId`
- `update<MetricName>(req, res)` — DB upsert metric (compute + save)

---

## Related Skills
- [tracking.md](./tracking.md) — spending and location data feed into metric calculations
