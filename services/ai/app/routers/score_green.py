from fastapi import APIRouter
from pydantic import BaseModel
from typing import List
from app.core_config import load_core

router = APIRouter()

# =========================
# CONFIG (core data from GREENMIND_CORE_DIR)
# =========================
CORE = load_core("green_score")
BASE_SCORE = CORE["base_score"]
CLASS_WEIGHTS = CORE["class_weights"]
RULES = CORE["rules"]

RECYCLABLE = set(CORE["groups"]["recyclable"])
PLASTIC = set(CORE["groups"]["plastic"])
HAZARD = set(CORE["groups"]["hazard"])
ORGANIC = set(CORE["groups"]["organic"])

# =========================
# SCHEMA
# =========================
class Item(BaseModel):
    name: str
    quantity: int

class ScoreRequest(BaseModel):
    current_score: float = BASE_SCORE
    items: List[Item]

# =========================
# UTILS
# =========================
def normalize_name(name: str):
    return name.strip().lower()

# =========================
# CALCULATE
# =========================
def calculate_delta(items: List[Item]):
    total_score = 0
    total_count = 0

    plastic_count = 0
    has_food = False
    has_recycle = False
    has_hazard = False

    reasons = []

    # ===== LOOP =====
    for item in items:
        name = normalize_name(item.name)
        quantity = item.quantity
        weight = CLASS_WEIGHTS.get(name, 0)

        score_change = weight * quantity
        total_score += score_change
        total_count += quantity

        # reason từng item
        if weight > 0:
            reasons.append(f"{quantity} x {name} → +{score_change} điểm (tốt)")
        elif weight < 0:
            reasons.append(f"{quantity} x {name} → {score_change} điểm (gây hại)")

        # group tracking
        if name in PLASTIC:
            plastic_count += quantity
        if name in ORGANIC:
            has_food = True
        if name in RECYCLABLE:
            has_recycle = True
        if name in HAZARD:
            has_hazard = True

    if total_count == 0:
        return 0, []

    # =========================
    # RULES
    # =========================

    # Rule 0: có nhựa là bị trừ
    if plastic_count > 0:
        penalty = plastic_count * RULES["plastic_penalty_per_item"]
        total_score -= penalty
        reasons.append(f"Có rác nhựa ({plastic_count} món) → -{penalty} điểm")

    # 1. nhiều nhựa (>50%)
    if plastic_count / total_count > RULES["plastic_ratio_threshold"]:
        total_score -= RULES["plastic_ratio_penalty"]
        reasons.append(f"Nhiều nhựa dùng một lần → -{RULES['plastic_ratio_penalty']} điểm")

    # 2. food + recyclable
    if has_food and has_recycle:
        total_score -= RULES["organic_contamination_penalty"]
        reasons.append(f"Rác hữu cơ làm bẩn rác tái chế → -{RULES['organic_contamination_penalty']} điểm")

    # 3. hazardous
    if has_hazard:
        total_score -= RULES["hazard_penalty"]
        reasons.append(f"Có rác nguy hại (pin/aerosol) → -{RULES['hazard_penalty']} điểm")

    # 4. phân loại tốt
    if has_recycle and plastic_count == 0 and not has_food:
        total_score += RULES["good_sorting_bonus"]
        reasons.append(f"Phân loại tốt, nhiều rác tái chế → +{RULES['good_sorting_bonus']} điểm")

    return total_score, reasons


@router.post("/score")
async def get_score(data: ScoreRequest):
    delta, reasons = calculate_delta(data.items)

    final_score = data.current_score + delta
    final_score = max(0, min(100, final_score))

    return {
        "previous_score": data.current_score,
        "delta": delta,
        "final_score": final_score,
        "reasons": reasons
    }


@router.get("/home")
async def home():
    return {"message": "ok"}