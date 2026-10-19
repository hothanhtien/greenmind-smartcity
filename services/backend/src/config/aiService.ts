import "./env";

// Base URL of the GreenMind AI service (FastAPI). Override with AI_SERVICE_URL, e.g. http://localhost:8000 for local runs.
export const AI_SERVICE_URL = (process.env.AI_SERVICE_URL || "https://ai-greenmind.khoav4.com").replace(/\/+$/, "");
