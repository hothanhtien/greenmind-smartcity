# Skill: Questions & Question Sets

## Files
- `src/routes/questionRoutes.ts`
- `src/routes/questionSetRoutes.ts`
- `src/controller/questionsController.ts`
- `src/controller/questionSetController.ts`
- Entities: `src/entity/questions.ts`, `src/entity/question_sets.ts`, `src/entity/question_options.ts`

---

## Routes — base: `/questions`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/questions/` | `jwt`, `staffOrAdmin` | `CreateQuestion()` |
| POST | `/questions/createQuestions` | `jwt`, `staffOrAdmin` | `createQuestions()` |
| POST | `/questions/survey-verify` | — | `surveyVerifyController.verifySurvey()` |
| GET | `/questions/` | `jwt` | `GetQuestions()` |
| GET | `/questions/survey` | `jwt` | `getSurveyQuestions()` |
| GET | `/questions/my-questions` | `jwt` | `GetQuestionsByOwner()` |
| GET | `/questions/template/:templateId` | `jwt` | `GetQuestionsByTemplate()` |
| GET | `/questions/owner/:ownerId` | `jwt` | `GetQuestionsByOwner()` |
| GET | `/questions/:id` | `jwt` | `GetQuestionById()` |
| PUT | `/questions/:id` | `jwt`, `staffOrAdmin` | `UpdateQuestion()` |
| DELETE | `/questions/:id` | `jwt`, `admin` | `DeleteQuestion()` |

---

## Routes — base: `/question-sets`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/question-sets/` | `jwt`, `staffOrAdmin` | `createQuestionSet()` |
| GET | `/question-sets/` | `jwt`, `staffOrAdmin` | `getQuestionSets()` |
| GET | `/question-sets/my-sets` | `jwt` | `getQuestionSetsByOwner()` |
| GET | `/question-sets/owner/:ownerId` | `jwt` | `getQuestionSetsByOwner()` |
| GET | `/question-sets/:id` | `jwt` | `getQuestionSetById()` |
| PUT | `/question-sets/:id` | `jwt` | `updateQuestionSet()` |
| DELETE | `/question-sets/:id` | `jwt` | `deleteQuestionSet()` |

---

## Controller Functions

### `questionsController.ts`
- `CreateQuestion()` — DB insert
- `createQuestions()` — bulk DB insert
- `GetQuestions()` — DB find all
- `getSurveyQuestions()` — filtered by user location & age
- `GetQuestionsByOwner()` — filtered by `req.user.userId` or `params.ownerId`
- `GetQuestionsByTemplate()` — filtered by `params.templateId`
- `GetQuestionById()` — DB find one
- `UpdateQuestion()` — DB update
- `DeleteQuestion()` — DB delete

### `questionSetController.ts`
- `createQuestionSet()` — DB insert
- `getQuestionSets()` — DB find all
- `getQuestionSetsByOwner()` — filtered by owner
- `getQuestionSetById()` — DB find one
- `updateQuestionSet()` — DB update
- `deleteQuestionSet()` — DB delete

---

## Related Skills
- [survey.md](./survey.md) — `verifySurvey()` at `POST /questions/survey-verify`
- [templates.md](./templates.md) — questions linked to templates
