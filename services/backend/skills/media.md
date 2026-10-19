# Skill: Media (OCR, Healthy Food, Brands)

Covers file-upload-based routes and brand data ingestion.

## Files
- `src/routes/ocrRoutes.ts` → `src/controller/ocrController.ts`
- `src/routes/healthyFoodRoutes.ts` → `src/controller/healthyFoodController.ts`
- `src/routes/brandRoutes.ts` → `src/controller/brandController.ts`
- Entities: `src/entity/invoice.ts`, `src/entity/brands.ts`

---

## Routes — base: `/ocr`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/ocr/` | `jwt`, `multer.single('file')` | `ocrController.processOCR()` |
| GET | `/ocr/invoices` | `jwt` | `ocrController.getInvoices()` |

**multer config:** memoryStorage, 10MB limit, image files only.

### `ocrController.ts`
- `processOCR(req, res)` — reads `req.file` (Buffer), sends to OCR service, saves result as invoice
- `getInvoices(req, res)` — DB fetch all invoices for `req.user.userId`

---

## Routes — base: `/healthy-food-ratio`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/healthy-food-ratio/` | `jwt`, `multer.single('file')` | `healthyFoodController.analyzeHealthyFood()` |

**multer config:** memoryStorage, 10MB limit, image files only.

### `healthyFoodController.ts`
- `analyzeHealthyFood(req, res)` — reads `req.file` (Buffer), calls vision/AI service, returns healthy food ratio

---

## Routes — base: `/brands`

| Method | Path | Middlewares | Handler |
|---|---|---|---|
| POST | `/brands/` | `jwt` | `brandController.postBrand()` |

### `brandController.ts`
- `postBrand(req, res)` — DB insert brand record

---

## Notes
- Both OCR and HealthyFood use `multer` memory storage — `req.file.buffer` is the raw image bytes.
- File filter: only `image/*` MIME types accepted.
