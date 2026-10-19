# Skill: Models

## Files
- `src/routes/modelRoutes.ts`
- `src/controller/modelController.ts`
- `src/controller/surveyVerifyController.ts` (shared)
- Entities: `src/entity/models.ts`, `src/entity/model_users.ts`

---

## Routes — base: `/models`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/models/create` | `jwt` | `modelController.createBehaviorModel()` |
| GET | `/models/getAll` | `jwt` | `modelController.getAllModels()` |
| GET | `/models/:id/feedbacks` | — | `surveyVerifyController.getFeedbacksByModelId()` |
| GET | `/models/feedbacks` | — | `surveyVerifyController.getFeedbacks()` |
| GET | `/models/:id` | `jwt` | `modelController.getModelById()` |

---

## Controller Functions — `src/controller/modelController.ts`

Exported as `{ modelController }`:

- `createBehaviorModel(req, res)`
  - calls: `modelMatcher` (utils), DB insert
- `getAllModels(req, res)`
  - DB find all models
- `getModelById(req, res)`
  - DB find one by `params.id`

---

## Related Skills
- [survey.md](./survey.md) — `surveyVerifyController` feeds into model feedbacks
- [assessments.md](./assessments.md) — behavior feedbacks are linked by `modelId`
- [utils.md](./utils.md) — `modelMatcher.ts`
