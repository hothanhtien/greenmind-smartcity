# Skill: Assessments

Covers: Big Five personality scores, Behaviors, Behavior Feedbacks, User Answers.

## Files
- `src/routes/bigFiveRoutes.ts` → `src/controller/bigFiveController.ts`
- `src/routes/behaviorRoutes.ts` → `src/controller/behaviorController.ts`
- `src/routes/behaviorFeedbackRoutes.ts` → `src/controller/behaviorFeedbackController.ts`
- `src/routes/userAnswersRoutes.ts` → `src/controller/userAnswersController.ts`
- Entities: `src/entity/big_five.ts`, `src/entity/behaviors.ts`, `src/entity/behavior_feedback.ts`, `src/entity/feedback.ts`, `src/entity/user_answers.ts`

---

## Routes — base: `/big-five`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/big-five/` | `jwt` | `submitBigFive()` |
| GET | `/big-five/user/:userId` | `jwt` | `getBigFiveByUserId()` |
| PUT | `/big-five/user/:userId` | `jwt` | `updateBigFive()` |
| DELETE | `/big-five/user/:userId` | `jwt` | `deleteBigFive()` |
| GET | `/big-five/` | `jwt`, `admin` | `getAllBigFive()` |

---

## Routes — base: `/behaviors`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/behaviors/` | `jwt`, `staffOrAdmin` | `createBehavior()` |
| GET | `/behaviors/` | `jwt` | `getAllBehaviors()` |
| GET | `/behaviors/:id` | `jwt` | `getBehaviorById()` |
| PUT | `/behaviors/:id` | `jwt`, `staffOrAdmin` | `updateBehaviorById()` |
| DELETE | `/behaviors/:id` | `jwt`, `admin` | `deleteBehaviorById()` |

---

## Routes — base: `/behavior-feedbacks`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| GET | `/behavior-feedbacks/` | `jwt` | `getAllBehaviorFeedback()` |
| GET | `/behavior-feedbacks/users` | `jwt` | `getMechanismFeedbacksAllUsers()` |
| GET | `/behavior-feedbacks/user/:userId` | `jwt` | `getMechanismFeedbacksByUser()` |
| GET | `/behavior-feedbacks/model/:modelId` | `jwt` | `getMechanismFeedbacksByModel()` |

---

## Routes — base: `/user-answers`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/user-answers/submit` | `jwt` | `submitUserAnswers()` |
| POST | `/user-answers/` | `jwt` | `createUserAnswer()` |
| GET | `/user-answers/get-user-answer-by-id/:questionId` | `jwt` | `getUserAnswerById()` |
| GET | `/user-answers/user/:userId` | `jwt` | `getUserAnswersByUserId()` |
| GET | `/user-answers/question/:questionId` | `jwt`, `staffOrAdmin` | `getUserAnswersByQuestionId()` |
| PUT | `/user-answers/:userId/:questionId` | `jwt` | `updateUserAnswer()` |
| DELETE | `/user-answers/:userId/:questionId` | `jwt` | `deleteUserAnswer()` |
| GET | `/user-answers/` | `jwt`, `admin` | `getAllUserAnswers()` |

---

## Related Skills
- [models.md](./models.md) — behavior model linked to feedbacks
- [survey.md](./survey.md) — survey verify processes user answers
