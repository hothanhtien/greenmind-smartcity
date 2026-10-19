package com.vodang.greenmind

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Pure-Kotlin multiplatform helpers that replace JVM-only [String.format].
 */

/** Format a [Double] to [decimals] decimal places. */
fun Double.fmt(decimals: Int): String {
    if (decimals <= 0) return this.roundToLong().toString()
    val factor = 10.0.pow(decimals)
    val rounded = (abs(this) * factor).roundToLong()
    val intPart = rounded / factor.toLong()
    val fracPart = (rounded % factor.toLong()).toString().padStart(decimals, '0')
    val sign = if (this < 0) "-" else ""
    return "$sign$intPart.$fracPart"
}

/** Format a [Float] to [decimals] decimal places. */
fun Float.fmt(decimals: Int): String = this.toDouble().fmt(decimals)

/** Multiplatform replacement for [String.format] supporting a subset of format specifiers
 *  commonly used in this project: `%s`, `%d`, `%02d`, `%05d`, `%.Nf`, `%%`. */
fun String.fmt(vararg args: Any?): String {
    val sb = StringBuilder()
    var i = 0
    var argIdx = 0
    while (i < length) {
        if (this[i] == '%' && i + 1 < length) {
            i++ // skip '%'
            // Literal %%
            if (this[i] == '%') { sb.append('%'); i++; continue }
            // Parse optional width / zero-padding
            var zeroPad = false
            var width = 0
            if (this[i] == '0') { zeroPad = true; i++ }
            while (i < length && this[i].isDigit()) { width = width * 10 + (this[i] - '0'); i++ }
            // Parse optional precision
            var precision = -1
            if (i < length && this[i] == '.') {
                i++; precision = 0
                while (i < length && this[i].isDigit()) { precision = precision * 10 + (this[i] - '0'); i++ }
            }
            // Conversion character
            val arg = args.getOrNull(argIdx++)
            when {
                i < length && this[i] == 'd' -> {
                    i++
                    val num = (arg as? Number)?.toLong() ?: 0L
                    var s = abs(num).toString()
                    if (zeroPad && s.length < width) s = s.padStart(width, '0')
                    else if (s.length < width) s = s.padStart(width, ' ')
                    if (num < 0) sb.append('-')
                    sb.append(s)
                }
                i < length && this[i] == 'f' -> {
                    i++
                    val num = (arg as? Number)?.toDouble() ?: 0.0
                    val dec = if (precision >= 0) precision else 6
                    sb.append(num.fmt(dec))
                }
                i < length && this[i] == 's' -> {
                    i++
                    sb.append(arg?.toString() ?: "null")
                }
                else -> { sb.append('%'); argIdx-- }
            }
        } else {
            sb.append(this[i]); i++
        }
    }
    return sb.toString()
}
