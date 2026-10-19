import json
import re
from fastapi import HTTPException
from app.models.gemini import get_gemini   # chỉnh path nếu cần
from app.core_config import load_prompt


# ================== PROMPT (RÚT GỌN – CHỐNG CẮT JSON) ==================

PROMPT_TEMPLATE = load_prompt("invoice_parse_gemini")


# ================== GEMINI CALL ==================

def _call_gemini(prompt: str, max_output_tokens: int = 4096) -> str:
    try:
        gemini = get_gemini()
        return gemini.chat_raw(
            system_prompt="Trả JSON hoá đơn, không text khác.",
            user_prompt=prompt,
            max_output_tokens=max_output_tokens
        ).strip()
    except Exception as e:
        raise HTTPException(status_code=502, detail=str(e))


# ================== JSON UTILS ==================

def _extract_json(raw: str) -> str:
    m = re.search(r"(\{.*\})", raw, flags=re.DOTALL)
    return m.group(1) if m else ""


# ================== MAIN PARSER ==================

def parse_invoice_text(ocr_text: str, retry: int = 2) -> dict:
    print("OCR TEXT:\n", ocr_text)

    prompt = PROMPT_TEMPLATE.format(ocr_text=ocr_text)
    raw = _call_gemini(prompt)

    print("RAW GEMINI:\n", raw)

    cleaned = raw.strip()
    json_text = cleaned if cleaned.startswith("{") else _extract_json(cleaned)

    try:
        return json.loads(json_text)

    except Exception as e:
        if retry > 0:
            print("[WARN] JSON lỗi / bị cắt → retry Gemini")
            return parse_invoice_text(ocr_text, retry - 1)

        raise HTTPException(
            status_code=500,
            detail=f"JSON lỗi sau retry: {e}"
        )
