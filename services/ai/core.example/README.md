# core.example

Placeholder core data so the AI service starts without the private GreenMind core.
It has the same file names and JSON schema as the real core, but **dummy values** — results
produced with it are not meaningful.

To run with the real core, set in `services/ai/.env`:

```
GREENMIND_CORE_DIR=/path/to/greenmind-core/ai
GREENMIND_MODEL_DIR=/path/to/greenmind-core/models
```

| File | Used by |
|---|---|
| `pollution_mapping.json` | `POST /predict-pollutant-impact` |
| `invoice_pollution.json`, `prompts/invoice_*.txt` | `POST /invoice-pollution`, `POST /ocr_text` |
| `green_score.json` | `POST /score` |
| `waste_categories.json` | `POST /detect-trash-ver2` |
| `mass_estimation.json` | `POST /total-mass` |
| `landfill_scoring.json` | `POST /predict_trash_seg` |
| `prompts/todo_subtasks.txt` | `POST /generate_subtasks` |
| `plugins/*.py` (private only) | OCEAN / behavior endpoints, see `docs/api/ai-openapi.json` |

Model weights (`taco_yolov8n_seg_best_po.pt`, `best_landfill_yolov8_seg.pt`, `best.pt`) are private
and are loaded from `GREENMIND_MODEL_DIR`.
