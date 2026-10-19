import json
from fastapi import HTTPException
from app.models.qwen import get_qwen
from app.core_config import load_prompt

_SCHEMA_DESC = load_prompt("invoice_schema_qwen")

def parse_invoice_text(ocr_text: str) -> dict:
    print(ocr_text)
    llm = get_qwen()
    prompt = load_prompt("invoice_parse_qwen").format(ocr_text=ocr_text).strip()

    resp = llm.chat(prompt, max_new_tokens=512).strip()
    try:
        return json.loads(resp)
    except Exception:
        raise HTTPException(status_code=500, detail=f"LLM trả về không phải JSON: {resp}")
