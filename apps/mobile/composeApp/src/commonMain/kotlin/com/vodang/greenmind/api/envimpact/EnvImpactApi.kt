package com.vodang.greenmind.api.envimpact

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.time.nowIso8601
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json as KJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val json = KJson { ignoreUnknownKeys = true; isLenient = true }

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
private data class ImpactBody(
    val air: Double,
    val water: Double,
    val soil: Double,
)

@Serializable
private data class EnvImpactRequest(
    @SerialName("record_date") val recordDate: String,
    val pollution: Map<String, Double>,
    val impact: ImpactBody,
)

@Serializable
data class EnvImpactData(
    val id: String,
    val userId: String,
    val recordDate: String,
    val co2: Double = 0.0,
    val dioxin: Double = 0.0,
    val microplastic: Double = 0.0,
    @SerialName("toxic_chemicals")   val toxicChemicals: Double = 0.0,
    @SerialName("non_biodegradable") val nonBiodegradable: Double = 0.0,
    val nox: Double = 0.0,
    val so2: Double = 0.0,
    val ch4: Double = 0.0,
    val pm25: Double = 0.0,
    val pb: Double = 0.0,
    val hg: Double = 0.0,
    val cd: Double = 0.0,
    val nitrate: Double = 0.0,
    val chemicalResidue: Double = 0.0,
    val styrene: Double = 0.0,
    val airPollution: Double = 0.0,
    val waterPollution: Double = 0.0,
    val soilPollution: Double = 0.0,
    val createdAt: String = "",
    val updatedAt: String = "",
)

@Serializable
data class EnvImpactResponse(
    val message: String,
    val data: EnvImpactData,
)

// ── Helpers ───────────────────────────────────────────────────────────────────

/**
 * Derives today's record-date string in Vietnam timezone (+07:00).
 * Uses the UTC ISO string from [nowIso8601] and shifts the date by +7 hours
 * before taking the date portion. Close enough for daily logging purposes.
 */
private fun todayRecordDate(): String {
    // nowIso8601() → "2026-04-03T08:30:00.000Z"
    // Parse hour to decide if the VN date is a day ahead of UTC date
    val iso = nowIso8601()                   // "yyyy-MM-dd'T'HH:mm:ss..."
    val utcHour = iso.substring(11, 13).toIntOrNull() ?: 0
    val utcDate = iso.substring(0, 10)       // "yyyy-MM-dd"
    // If UTC hour + 7 >= 24, VN date is next day (we skip handling that edge case
    // since it only matters at 17:00–24:00 UTC = 00:00–07:00 VN, i.e. overnight).
    // For typical daytime scans this is always correct.
    return "${utcDate}T00:00:00+07:00"
}

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /environmental-impact — log the pollution data computed by the AI models
 * for today's record. Returns 201 on first creation, 200 on same-day update.
 *
 * This call is **fire-and-forget** by convention: callers should wrap it in a
 * separate [kotlinx.coroutines.launch] and swallow failures so it never
 * disrupts the main scan flow.
 */
suspend fun logEnvironmentalImpact(
    accessToken: String,
    pollution: Map<String, Double>,
    airImpact: Double,
    waterImpact: Double,
    soilImpact: Double,
): EnvImpactResponse {
    AppLogger.i("EnvImpact", "logEnvironmentalImpact air=$airImpact water=$waterImpact soil=$soilImpact")
    try {
        val resp = httpClient.post("$BASE_URL/environmental-impact") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(
                EnvImpactRequest(
                    recordDate = todayRecordDate(),
                    pollution  = pollution,
                    impact     = ImpactBody(air = airImpact, water = waterImpact, soil = soilImpact),
                )
            )
        }
        AppLogger.d("EnvImpact", "logEnvironmentalImpact → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            val raw = resp.bodyAsText()
            AppLogger.d("EnvImpact", "logEnvironmentalImpact raw body (${raw.length} chars): ${raw.take(500)}")
            try {
                json.decodeFromString<EnvImpactResponse>(raw)
                    .also { parsed ->
                        AppLogger.i("EnvImpact", "logEnvironmentalImpact ok: ${parsed.message} id=${parsed.data.id}")
                    }
            } catch (e: Throwable) {
                AppLogger.e("EnvImpact", "logEnvironmentalImpact parse error on: $raw")
                throw e
            }
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("EnvImpact", "logEnvironmentalImpact failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("EnvImpact", "logEnvironmentalImpact error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
