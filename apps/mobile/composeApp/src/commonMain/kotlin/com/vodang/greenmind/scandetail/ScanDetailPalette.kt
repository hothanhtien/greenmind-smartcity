package com.vodang.greenmind.scandetail

import androidx.compose.ui.graphics.Color

// ── Shared palette for scan detail components ──────────────────────────────────

// Status colors
val scanGreen = Color(0xFF2E7D32)   // stepDone, completed
val scanGray = Color(0xFFBDBDBD)   // stepPending
val scanGreenBg = Color(0xFFE8F5E9)  // stepBg

// Eco score
val ecoGreen700 = Color(0xFF2E7D32)
val ecoGreen50 = Color(0xFFE8F5E9)
val ecoRed600 = Color(0xFFDC2626)
val ecoRed50 = Color(0xFFFEF2F2)

// Pollutant impact
val pollutantRed = Color(0xFFD32F2F)
val pollutantOrange = Color(0xFFF57C00)
val pollutantGreen = Color(0xFF2E7D32)

// Neutral
val neutralGray400 = Color(0xFF9CA3AF)
val neutralGray700 = Color(0xFF374151)
val neutralGray200 = Color(0xFFEEEEEE)
val neutralGray100 = Color(0xFFF5F5F5)

// Blue (for mass)
val massBlue = Color(0xFF1565C0)
val massBlueBg = Color(0xFFE3F2FD)

// Category colors
fun categoryColor(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFF1565C0)
    "residual"   -> Color(0xFF6D4C41)
    "organic"    -> Color(0xFF2E7D32)
    "hazardous"  -> Color(0xFFD32F2F)
    else         -> Color(0xFF616161)
}

fun categoryBg(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFFE3F2FD)
    "residual"   -> Color(0xFFEFEBE9)
    "organic"    -> Color(0xFFE8F5E9)
    "hazardous"  -> Color(0xFFFFEBEE)
    else         -> Color(0xFFF5F5F5)
}