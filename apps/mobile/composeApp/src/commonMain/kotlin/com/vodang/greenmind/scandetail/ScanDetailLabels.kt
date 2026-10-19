package com.vodang.greenmind.scandetail

import androidx.compose.ui.graphics.Color

// ── Pollutant label mapping ───────────────────────────────────────────────────

/** Raw pollutant key → friendly display name */
val pollutantLabel = mapOf(
    "CO2"               to "CO₂",
    "dioxin"            to "Dioxin",
    "microplastic"      to "Microplastic",
    "toxic_chemicals"   to "Toxic chemicals",
    "non_biodegradable" to "Non-biodegradable",
    "NOx"               to "NOₓ",
    "SO2"               to "SO₂",
    "CH4"               to "CH₄",
    "PM2.5"             to "PM2.5",
    "Pb"                to "Lead (Pb)",
    "Hg"                to "Mercury (Hg)",
    "Cd"                to "Cadmium (Cd)",
    "nitrate"           to "Nitrate",
    "chemical_residue"  to "Chemical residue",
    "styrene"           to "Styrene",
)

/** All pollutant keys in display order */
val allPollutantKeys = listOf(
    "CO2", "CH4", "PM2.5", "NOx", "SO2",
    "Pb", "Hg", "Cd",
    "nitrate", "chemical_residue", "microplastic",
    "dioxin", "toxic_chemicals", "non_biodegradable", "styrene",
)

/** Get pollutant display name, fallback to key */
fun getPollutantLabel(key: String): String = pollutantLabel[key] ?: key

/** Get pollutant color based on severity (value vs threshold) */
fun getPollutantColor(value: Double): Color {
    return when {
        value > 0.05 -> pollutantRed
        value > 0.01 -> pollutantOrange
        else -> pollutantGreen
    }
}

/** Get impact color based on normalized percentage (0-100) */
fun getImpactColor(normalizedPercent: Double): Color {
    return when {
        normalizedPercent < 20 -> pollutantGreen
        normalizedPercent < 50 -> pollutantOrange
        else -> pollutantRed
    }
}