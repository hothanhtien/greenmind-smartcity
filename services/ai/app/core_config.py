"""Loads GreenMind core data (coefficients, weights, prompts, model weights) from outside the repo.

The real core files are private and are never committed. Point GREENMIND_CORE_DIR and
GREENMIND_MODEL_DIR at them; without GREENMIND_CORE_DIR the service falls back to
`core.example/`, which has the same schema but placeholder values.
"""
import json
import logging
import os
from functools import lru_cache
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

logger = logging.getLogger("greenmind.core")

SERVICE_ROOT = Path(__file__).resolve().parent.parent
EXAMPLE_CORE_DIR = SERVICE_ROOT / "core.example"


def _resolve_dir(env_name: str, default: Path) -> Path:
    value = os.getenv(env_name)
    return Path(value).expanduser().resolve() if value else default


CORE_DIR = _resolve_dir("GREENMIND_CORE_DIR", EXAMPLE_CORE_DIR)
MODEL_DIR = _resolve_dir("GREENMIND_MODEL_DIR", SERVICE_ROOT / "models")

if CORE_DIR == EXAMPLE_CORE_DIR:
    logger.warning("GREENMIND_CORE_DIR is not set: using placeholder values from core.example/")


@lru_cache(maxsize=None)
def load_core(name: str) -> dict:
    """Return the parsed JSON file `<CORE_DIR>/<name>.json`."""
    with open(CORE_DIR / f"{name}.json", encoding="utf-8") as f:
        return json.load(f)


@lru_cache(maxsize=None)
def load_prompt(name: str) -> str:
    """Return the prompt template `<CORE_DIR>/prompts/<name>.txt`."""
    return (CORE_DIR / "prompts" / f"{name}.txt").read_text(encoding="utf-8")


def core_path(*parts: str) -> Path:
    """Path to any other core asset, e.g. core_path("ocean", "templates.json")."""
    return CORE_DIR.joinpath(*parts)


def model_path(filename: str) -> str:
    """Path to a trained model weight file kept in GREENMIND_MODEL_DIR."""
    path = MODEL_DIR / filename
    if not path.exists():
        raise FileNotFoundError(f"Model weights not found: {path} (set GREENMIND_MODEL_DIR)")
    return str(path)
