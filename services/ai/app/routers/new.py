from fastapi import APIRouter, UploadFile, File, Form, HTTPException
from typing import Optional
from collections import defaultdict
import math
import json
import re

# ===== OCR =====
from app.services.ocr_text import ocr_text_from_source, ocr_text_from_upload
from app.services.orc_parse_gemini import parse_invoice_text

# ===== LLM =====
from app.models.open_ai_llm import get_openai_llm
from app.core_config import load_core, load_prompt
router = APIRouter()

# =========================
# CLASS NAMES (🔥 NEW)
# =========================
CLASS_NAMES = {
    0:"Aluminium foil",1:"Battery",2:"Aluminium blister pack",3:"Carded blister pack",
    4:"Other plastic bottle",5:"Clear plastic bottle",6:"Glass bottle",7:"Plastic bottle cap",
    8:"Metal bottle cap",9:"Broken glass",10:"Food Can",11:"Aerosol",12:"Drink can",
    13:"Toilet tube",14:"Other carton",15:"Egg carton",16:"Drink carton",17:"Corrugated carton",
    18:"Meal carton",19:"Pizza box",20:"Paper cup",21:"Disposable plastic cup",22:"Foam cup",
    23:"Glass cup",24:"Other plastic cup",25:"Food waste",26:"Glass jar",27:"Plastic lid",
    28:"Metal lid",29:"Other plastic",30:"Magazine paper",31:"Tissues",32:"Wrapping paper",
    33:"Normal paper",34:"Paper bag",35:"Plastified paper bag",36:"Plastic film",
    37:"Six pack rings",38:"Garbage bag",39:"Other plastic wrapper",40:"Single-use carrier bag",
    41:"Polypropylene bag",42:"Crisp packet",43:"Spread tub",44:"Tupperware",
    45:"Disposable food container",46:"Foam food container",47:"Other plastic container",
    48:"Plastic glooves",49:"Plastic utensils",50:"Pop tab",51:"Rope & strings",
    52:"Scrap metal",53:"Shoe",54:"Squeezable tube",55:"Plastic straw",
    56:"Paper straw",57:"Styrofoam piece",58:"Unlabeled litter",59:"Cigarette"
}

# =========================
# POLLUTION MAPPING + PROMPTS (core data from GREENMIND_CORE_DIR)
# =========================
CORE = load_core("invoice_pollution")
ALPHA = CORE["alpha"]
BETA = CORE["beta"]
ALL_POLLUTANTS = CORE["pollutants"]

PROMPT_PARSE = load_prompt("invoice_parse")
PROMPT_MAP = load_prompt("invoice_map_taco")

# =========================
# LLM
# =========================
def call_llm(prompt: str) -> str:
    llm = get_openai_llm()
    return llm.chat_raw(
        system_prompt="Return valid JSON only",
        user_prompt=prompt,
        max_output_tokens=1024
    ).strip()

def extract_json(raw: str):
    m = re.search(r"(\{.*\})", raw, re.DOTALL)
    return m.group(1) if m else "{}"

def safe_json_load(raw: str):
    try:
        return json.loads(raw)
    except Exception as e:
        print("[JSON ERROR 1]", e)

    # 🔥 extract JSON block
    raw = extract_json(raw)

    # 🔥 fix lỗi phổ biến
    raw = raw.replace("\n", " ")
    raw = re.sub(r",\s*}", "}", raw)   # remove trailing comma
    raw = re.sub(r",\s*]", "]", raw)

    # 🔥 fix thiếu dấu "
    raw = re.sub(r'("raw_name":\s*"[^"]+)(?=\s*,|\s*})', r'\1"', raw)

    try:
        return json.loads(raw)
    except Exception as e:
        print("[JSON ERROR 2]", e)

    return {"items": []}
def parse_invoice(ocr_text: str):
    print("\n========== ocr text ==========")
    print(ocr_text)
    print("=============================\n")
    raw = call_llm(PROMPT_PARSE.format(ocr=ocr_text))
    print("\n========== RAW LLM ==========")
    print(raw)
    print("=============================\n")
    return safe_json_load(raw)

def llm_map_item(name: str) -> int:
    try:
        raw = call_llm(PROMPT_MAP.format(name=name))
        return int(re.findall(r"\d+", raw)[0])
    except:
        return 58

# =========================
# BUILD (🔥 UPDATED)
# =========================
def build_detections(items):
    detections = []
    enriched_items = []

    for item in items:
        name = item.get("raw_name", "")
        quantity = item.get("quantity", 1)

        class_id = llm_map_item(name)

        # dùng cho pollution
        if class_id != 58:
            detections.append({
                "class": class_id,
                "quantity": quantity
            })

        # dùng cho output (🔥 quan trọng)
        enriched_items.append({
            "raw_name": name,
            "quantity": quantity,
            "class_id": class_id,
            "class_name": CLASS_NAMES.get(class_id, "Unknown")
        })

    return detections, enriched_items

# =========================
# POLLUTION
# =========================
def compute_pollution(detections):
    pollution = defaultdict(float)

    for obj in detections:
        cls = str(obj["class"])
        quantity = obj["quantity"]

        alpha = ALPHA.get(cls, 1.0)
        beta = BETA.get(cls, {})

        for p, w in beta.items():
            pollution[p] += quantity * alpha * w

    for p in ALL_POLLUTANTS:
        pollution[p] = pollution.get(p, 0.0)

    return dict(pollution)

def normalize(pollution):
    total = sum(pollution.values()) + 1e-6
    return {k: v/total for k,v in pollution.items()}

def classify(pollution):
    air = CORE["impact_groups"]["air"]
    water = CORE["impact_groups"]["water"]
    soil = CORE["impact_groups"]["soil"]

    def score(keys):
        vals = [pollution[k] for k in keys if pollution[k] > 0]
        if not vals: return 0
        return sum(vals)/len(vals)

    return {
        "air": score(air),
        "water": score(water),
        "soil": score(soil)
    }

# =========================
# API
# =========================
@router.post("/invoice-pollution")
async def invoice_pollution(
    file: Optional[UploadFile] = File(None),
    url: Optional[str] = Form(None)
):
    if not file and not url:
        raise HTTPException(400, "Cần file hoặc url")

    # OCR
    if file:
        content = await file.read()
        text = ocr_text_from_upload(content, file.filename)
    else:
        text = ocr_text_from_source(url)

    if not text.strip():
        raise HTTPException(500, "OCR fail")
    print('text ocr', text)
    # Parse
    parsed = parse_invoice(text)
    items = parsed.get("items", []) 
    print('parsed', parsed)

    # Mapping
    detections, enriched_items = build_detections(items)

    if not detections:
        return {
            "items": enriched_items,
            "pollution": {k:0 for k in ALL_POLLUTANTS},
            "impact": {"air":0,"water":0,"soil":0}
        }

    # Compute
    pollution = compute_pollution(detections)
    pollution = normalize(pollution)
    impact = classify(pollution)

    return {
        "items": enriched_items,   # 🔥 đã map
        "pollution": pollution,
        "impact": impact
    }