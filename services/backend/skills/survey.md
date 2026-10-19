# Skill: Survey

Covers: Scenarios Survey, Survey Verification, Pre-App Survey.

## Files
- `src/routes/scenariosSurveyRoutes.ts` → `src/controller/surveyScenarioController.ts`
- `src/routes/preAppSurveyRoutes.ts` → `src/controller/preAppSurveyController.ts`
- `src/controller/surveyVerifyController.ts` (used in `/questions` and `/models`)
- `src/utils/verifySurveyHelper.ts`
- Entities: `src/entity/survey_scenario.ts`, `src/entity/scenario_assignments.ts`, `src/entity/simulated_survey.ts`, `src/entity/pre_app_survey.ts`, `src/entity/segments.ts`

---

## Routes — base: `/scenarios-survey`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| GET | `/scenarios-survey/get-survey-scenario` | `jwt` | `GetSurveyScenarios()` |
| POST | `/scenarios-survey/create-survey-scenario` | `jwt` | `CreateSurveyScenario()` |
| PUT | `/scenarios-survey/attach-question/:id` | `jwt` | `AttachQuestions()` |
| DELETE | `/scenarios-survey/delete-survey-scenarios/:id` | `jwt` | `DeleteSurveyScenario()` |
| POST | `/scenarios-survey/simulate-scenario/:id` | `jwt` | `SimulateScenario()` |
| GET | `/scenarios-survey/get-simulated/:id` | `jwt` | `GetSimulatedDetails()` |
| GET | `/scenarios-survey/get-all-simulated-scenarios` | `jwt` | `GetAllSimulatedScenarios()` |
| GET | `/scenarios-survey/get-user-survey-question` | `jwt` | `GetUserQuestionsSurvey()` |
| GET | `/scenarios-survey/get-user-question-set-survey` | `jwt` | `GetUserQuestionSetSurveys()` |
| GET | `/scenarios-survey/get-all-user-question` | `jwt` | `GetAllQuestionByUser()` |

---

## Routes — base: `/pre-app-survey`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/pre-app-survey/submit` | `jwt` | `submitPreAppSurvey()` |
| PUT | `/pre-app-survey/parameters` | `jwt` | `updateParameters()` |
| GET | `/pre-app-survey/:userId` | `jwt` | `getPreAppSurvey()` |
| GET | `/pre-app-survey/` | `jwt`, `admin` | `getAllPreAppSurveys()` |
| DELETE | `/pre-app-survey/:userId` | `jwt` | `deletePreAppSurvey()` |

---

## Survey Verify (cross-cutting)

### `surveyVerifyController.ts`
| Used In Route | Handler |
|---|---|
| `POST /questions/survey-verify` | `verifySurvey()` |
| `GET /models/:id/feedbacks` | `getFeedbacksByModelId()` |
| `GET /models/feedbacks` | `getFeedbacks()` |

- `verifySurvey()` — calls `verifySurveyHelper` (scoring logic)
- `getFeedbacksByModelId()` — DB lookup feedback by model
- `getFeedbacks()` — DB lookup all feedbacks

---

## Related Skills
- [questions.md](./questions.md) — `POST /questions/survey-verify`
- [models.md](./models.md) — feedbacks attached to models
- [assessments.md](./assessments.md) — user answers drive survey results
