"""Registers private core routers (OCEAN / behavior engine) kept in <GREENMIND_CORE_DIR>/plugins.

Each plugin is a Python module exposing a FastAPI `router`. They are not part of the public
repo; their HTTP contract is documented in docs/api/openapi.json.
"""
import importlib.util
import logging

from fastapi import FastAPI

from app.core_config import core_path

logger = logging.getLogger("greenmind.plugins")


def include_core_plugins(app: FastAPI) -> list[str]:
    plugin_dir = core_path("plugins")
    if not plugin_dir.is_dir():
        logger.warning("No core plugins found at %s: OCEAN/behavior endpoints are disabled", plugin_dir)
        return []

    loaded = []
    for path in sorted(plugin_dir.glob("*.py")):
        if path.name.startswith("_"):
            continue
        spec = importlib.util.spec_from_file_location(f"greenmind_core_plugins.{path.stem}", path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        router = getattr(module, "router", None)
        if router is None:
            continue
        if getattr(module, "INCLUDE", True):
            app.include_router(router)
            loaded.append(path.stem)
    logger.info("Loaded core plugins: %s", ", ".join(loaded))
    return loaded
