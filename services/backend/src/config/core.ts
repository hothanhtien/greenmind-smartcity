import fs from "fs";
import path from "path";
import "./env";

// Private GreenMind core data (behavior parameters, business rules). Set GREENMIND_CORE_DIR to the
// private core; without it the placeholder values in core.example/ are used.
const CORE_DIR = process.env.GREENMIND_CORE_DIR || path.resolve(__dirname, "../../core.example");

if (!process.env.GREENMIND_CORE_DIR) {
    console.warn("[core] GREENMIND_CORE_DIR is not set: using placeholder values from core.example/");
}

function loadCore<T>(name: string): T {
    return JSON.parse(fs.readFileSync(path.join(CORE_DIR, `${name}.json`), "utf8")) as T;
}

export interface MetricParams {
    weight: number;
    direction: "up" | "down";
    sigma_r: number;
    alpha: number;
}

export const BEHAVIOR_PARAMS = loadCore<Record<string, MetricParams>>("behavior_params");
export const BUSINESS_RULES = loadCore<{ waste_fee_vnd_per_kg: number }>("business_rules");
