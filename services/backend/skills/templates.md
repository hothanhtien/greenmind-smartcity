# Skill: Templates & Traits

## Files
- `src/routes/templateRoutes.ts`
- `src/routes/traitRoutes.ts`
- `src/controller/templateController.ts`
- `src/controller/traitController.ts`
- Entities: `src/entity/templates.ts`, `src/entity/template_answers.ts`, `src/entity/traits.ts`

---

## Routes — base: `/templates`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/templates/create` | `jwt` | `createTemplate()` |
| POST | `/templates/createTemplates` | `jwt` | `createTemplates()` |
| GET | `/templates/getById/:id` | `jwt` | `getTemplateById()` |
| GET | `/templates/getAll` | `jwt` | `getAllTemplates()` |
| PUT | `/templates/update` | `jwt` | `updateTemplateById()` |
| DELETE | `/templates/delete/:id` | `jwt` | `deleteTemplateById()` |

---

## Routes — base: `/traits`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/traits/trait/create` | `jwt` | `createTrait()` |
| GET | `/traits/trait/:id` | `jwt` | `getTraitById()` |
| PUT | `/traits/trait/update/:id` | `jwt` | `updateTraitById()` |
| DELETE | `/traits/trait/delete/:id` | `jwt` | `deleteTraitById()` |

---

## Controller Functions

### `templateController.ts`
- `createTemplate()` — DB insert single
- `createTemplates()` — bulk DB insert
- `getTemplateById()` — DB find one
- `getAllTemplates()` — DB find all
- `updateTemplateById()` — DB update
- `deleteTemplateById()` — DB delete

### `traitController.ts`
- `createTrait()` — DB insert
- `getTraitById()` — DB find one
- `updateTraitById()` — DB update
- `deleteTraitById()` — DB delete

---

## Related Skills
- [questions.md](./questions.md) — questions are linked to templates
