# GreenMind — TODO / Unfinished Features

Generated from codebase scan on 2026-03-29.

---

## API / Backend Integration

### Bill History — ✅ DONE (`api/ocr/OcrApi.kt`, `bill/BillListScreen.kt`)
- `GET /ocr/invoices` → `List<InvoiceDto>` loaded on screen open
- `InvoiceTotals` uses `String?` to match the backend's string-encoded number fields
- `BillListScreen` shows loading/error states + invoice list with green ratio badge
- Tap any card to open a full detail bottom sheet (items table + totals + meta)

### Bill Analysis — ✅ DONE (`api/ocr/OcrApi.kt`, `api/bill/BillAnalysisApi.kt`)
- Real OCR endpoint wired: `POST /ocr` (multipart `file`) → `OcrResponse`
- `OcrApi.kt` uses a dedicated HTTP client with 120 s timeout
- `BillAnalysisApi.kt` maps `OcrResponse` → `BillAnalysisResult` for BillStore
- `BillScanScreen.android.kt` calls `ocrBill()` directly and shows rich result UI

### Meal Analysis (`api/meal/MealAnalysisApi.kt`)
- Replace `mockMeals` list and `analyzeMeal()` random pick with a real HTTP call
- Expected: `POST /scan/meal` (multipart image) → `MealAnalysisResult { plantRatio, description }`
- Remove `delay(1_500)` once real endpoint is wired

### Environmental Impact (`home/components/EnvImpactCard.kt`)
- Replace all `mockItems`, `mockAirPollution`, `mockWaterPollution`, `mockSoilPollution`, `mockActivePollutants` with real DTO
- Expected: response from YOLO-detect endpoint `{ items[], total_objects, pollution{}, impact{} }`
- Change signature to `EnvImpactCard(result: YoloScanResult)` once DTO exists
- Filter active pollutants dynamically (keys where `value > 0`)
- Replace hardcoded tip string with `LocalAppStrings`

---

## Authentication

### Forgot Password (`LoginScreen.kt`)
- `TextButton(onClick = { })` for "Forgot password" does nothing
- Expected: navigate to `ForgotPasswordScreen` or show dialog → `POST /auth/forgot-password { email }`

### Google Sign-In (`LoginScreen.kt`)
- "Continue with Google" button calls `onLoginSuccess()` directly — **bypasses all auth**
- Expected: integrate Google Identity SDK, obtain `idToken`, call `POST /auth/login/google { idToken }` → `LoginEmailResponse`

---

## Household Dashboard

### Waste Sort button (`home/components/HouseholdDashboard.kt`)
- Feature button `📷 Waste Sort` has an empty `onClick = { }`
- Should navigate to `WasteSortScreen` (same as the `onWasteSortClick` parameter already wired in `HomeScreen`)

### Energy Screen (`energy/EnergyScreen.kt`)
- `weekValues` and `monthTotal` are hardcoded static floats
- `"▲5% vs yesterday"` is a hardcoded raw string (not in i18n)
- Expected: `GET /energy/readings?range=week` and `?range=month`; wire into an `EnergyStore`; compute delta vs yesterday dynamically

### Environmental Impact Card (`home/components/HouseholdDashboard.kt`)
- `EnvImpactCard()` is called with no arguments — uses internal mock data
- Wire with real `YoloScanResult` once the scan API is integrated

---

## Collector Dashboard

### Route / collection points (`home/components/CollectorDashboard.kt`)
- `points` is a hardcoded list of 6 `WastePoint` objects
- Expected: `GET /collector/route` → today's assigned points; store in `CollectorStore`

### Route distance (`home/components/CollectorDashboard.kt`)
- `MetricCard` shows hardcoded `"8.2 km"` for route distance
- Calculate from actual assigned `WastePoint` coordinates

### Heatmap feature button (`home/components/CollectorDashboard.kt`)
- `FeatureButton("🗺️" …) { }` — empty handler
- Navigate to a full-screen `GarbageHeatmapScreen`

### Schedule feature button (`home/components/CollectorDashboard.kt`)
- `FeatureButton("📅" …) { }` — empty handler
- Navigate to a shift/collection schedule calendar screen

### Check-in button (`home/components/CheckInCard.kt`)
- `Surface(onClick = { })` — does nothing on tap
- Expected: `POST /collector/checkin { pointId, lat, lng, timestamp }`; on success mark point collected in store and refresh route

### Heatmap data (`home/components/GarbageMapView.kt`, `GarbageHeatmapCard.kt`)
- `sampleGarbageMapPoints` — 12 hardcoded Da Nang coordinates
- Expected: `GET /waste-reports` → derive `{ lat, lng, intensity }` from report coordinates + `wasteKg`

---

## Volunteer Dashboard

### Event list (`home/components/VolunteerDashboard.kt`)
- `activeEvents` (2 items) and `upcomingEvents` (3 items) are hardcoded with fixed dates
- Expected: `GET /volunteer/events?status=active` and `?status=upcoming`; store in `VolunteerStore`

### Volunteer stats metrics (`home/components/VolunteerDashboard.kt`)
- Hours, events joined, and points values come from hardcoded i18n strings (`volunteerHoursValue` etc.)
- Expected: `GET /volunteer/stats` → real values; pass into `MetricCard`

### Join event button (`home/components/VolunteerDashboard.kt`)
- `onClick = { registered = true }` — only flips local UI state, no API call
- Expected: `POST /volunteer/events/{id}/register`; update event list from store on success

---

## Top Bar

### Camera shortcut (`home/HomeScreen.kt`)
- `onCameraClick = { /*TODO*/ }` — does nothing
- Decide: open `WasteSortScreen`, or a new quick-capture flow for YOLO scan

---

## Todos Screen

### AI subtask generation (`todos/TodoScreen.kt`)
- `✨` wand button: `onClick = { /* TODO: AI auto-generate subtasks */ }`
- Expected: `POST /todos/{id}/ai-expand` → `List<TodoItem>`; call `TodoStore.addChildren(id, list)`

---

## Blog Screen (`blog/BlogScreen.kt`)
- Entire screen is an empty-state placeholder
- Expected: `GET /blog/posts` → `List<BlogPostDto> { title, summary, imageUrl, publishedAt }`
- Build `BlogPostCard` composable + `BlogPostScreen` detail view

---

## UI / Polish

### OceanScoreCard empty slot (`home/components/OceanScoreCard.kt`)
- Right side of the header row has a blank `Box(52.dp)` placeholder
- Add a total OCEAN score badge or a trend arrow/dial

### Hardcoded Vietnamese string (`wastereport/WasteReportScanScreen.android.kt`)
- `"Đang lấy vị trí…"` is a raw string literal, not in `AppStrings`
- Add `wasteReportLocating` key to `AppStrings`, `EnStrings`, and `ViStrings`
