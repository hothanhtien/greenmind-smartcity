from fastapi import APIRouter, UploadFile, File
from ultralytics import YOLO
import numpy as np
import cv2
from collections import defaultdict
import os
import uuid
import cloudinary
import cloudinary.uploader
from dotenv import load_dotenv
import math
from fastapi.encoders import jsonable_encoder
from app.core_config import load_core, model_path
# =========================
# Init router
# =========================
router = APIRouter()

# =========================
# Load ENV
# =========================
load_dotenv()

cloudinary.config(
    cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME"),
    api_key=os.getenv("CLOUDINARY_API_KEY"),
    api_secret=os.getenv("CLOUDINARY_API_SECRET")
)

# =========================
# Load YOLO model
# =========================
MODEL_PATH = model_path("taco_yolov8n_seg_best_po.pt")
model = YOLO(MODEL_PATH)

# =========================
# POLLUTION MAPPING (core data from GREENMIND_CORE_DIR)
# =========================
CORE = load_core("pollution_mapping")
ALPHA = CORE["alpha"]
BETA = CORE["beta"]
ALL_POLLUTANTS = CORE["pollutants"]

# =========================
# Pollution pipeline
# =========================
def compute_pollution(detections):
    pollution = defaultdict(float)
    for obj in detections:
        cls = str(obj["class"])
        area = obj["area"]
        alpha = ALPHA.get(cls, 1.0)
        beta = BETA.get(cls, {})
        for pollutant, weight in beta.items():
            pollution[pollutant] += area * alpha * weight
    for p in ALL_POLLUTANTS:
        if p not in pollution:
            pollution[p] = 0.0
    return dict(pollution)

def normalize_pollution(pollution):
    max_val = max(pollution.values()) + 1e-6
    return {k: v / max_val for k, v in pollution.items()}

def log_scale(pollution):
    return {k: math.log(1 + v) for k, v in pollution.items()}

def classify_environment(pollution):
    groups = CORE["impact_groups"]
    air_pollutants = groups["air_pollution"]
    water_pollutants = groups["water_pollution"]
    soil_pollutants = groups["soil_pollution"]

    def score(keys):
        vals = [pollution.get(p, 0) for p in keys if pollution.get(p, 0) > 0]
        if not vals:
            return 0.0

        avg = sum(vals) / len(vals)
        coverage = len(vals) / len(keys)   # 🔥 quan trọng

        return avg * coverage

    return {
        "air_pollution": score(air_pollutants),
        "water_pollution": score(water_pollutants),
        "soil_pollution": score(soil_pollutants)
    }

def sigmoid_scale(pollution, k=0.00001, midpoint=50000):
    scaled = {}
    for key, v in pollution.items():
        if v == 0:
            scaled[key] = 0.0   # 🔥 giữ nguyên
        else:
            x = k * (v - midpoint)
            scaled[key] = 1 / (1 + math.exp(-x))
    return scaled

def predict_pollution(detections):
    pollution = compute_pollution(detections)
    pollution = normalize_pollution(pollution)
    pollution = sigmoid_scale(pollution, k=CORE["sigmoid"]["k"], midpoint=CORE["sigmoid"]["midpoint"])
    pollution = log_scale(pollution)
    impact = classify_environment(pollution)
    return {"pollution": pollution, "impact": impact}

# =========================
# API predict pollutant
# =========================
@router.post("/predict-pollutant-impact")
async def predict_pollutant_impact(file: UploadFile = File(...)):
    # 1. đọc ảnh
    contents = await file.read()
    np_arr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(np_arr, cv2.IMREAD_COLOR)

    # 2. detect
    results = model(img, conf=CORE["detect_conf"])
    result = results[0]
    names = model.names  # dict {id: name}
    # tạo dict đảo ngược name -> id
    name_to_id = {v: k for k, v in names.items()}

    # 3. xử lý output
    item_data = defaultdict(lambda: {"quantity": 0, "area": 0})
    if result.masks is not None:
        masks = result.masks.data.cpu().numpy()
        class_ids = result.boxes.cls.cpu().numpy().astype(int)

        for i, class_id in enumerate(class_ids):
            mask = masks[i]
            area = int(mask.sum())
            name = names[class_id]
            item_data[name]["quantity"] += 1
            item_data[name]["area"] += area

    items = [
        {
            "name": name,
            "quantity": int(val["quantity"]),
            "area": int(val["area"])
        }
        for name, val in item_data.items()
    ]

    # 4. compute pollution
    detections_list = [{"class": int(name_to_id[name]), "area": val["area"]} for name, val in item_data.items()]
    pollution_result = predict_pollution(detections_list)

    # 5. encode ảnh
    annotated_img = result.plot()
    _, buffer = cv2.imencode(".jpg", annotated_img)
    upload_result = cloudinary.uploader.upload(
        buffer.tobytes(),
        public_id=f"detect/{uuid.uuid4().hex}",
        folder="yolo_detect",
        resource_type="image"
    )

    # 6. ensure native types
    pollution = {k: float(v) for k, v in pollution_result["pollution"].items()}
    impact = {k: float(v) for k, v in pollution_result["impact"].items()}

    # 7. return
    return jsonable_encoder({
        "items": items,
        "total_objects": len(result.boxes) if result.boxes else 0,
        "image_url": upload_result["secure_url"],
        "pollution": pollution,
        "impact": impact
    })