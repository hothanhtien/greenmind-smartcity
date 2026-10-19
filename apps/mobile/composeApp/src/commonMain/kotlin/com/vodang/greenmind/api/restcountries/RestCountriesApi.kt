package com.vodang.greenmind.api.restcountries

import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val REST_COUNTRIES_BASE = "https://restcountries.com/v3.1"

@Serializable
data class CountryName(
    val common: String,
    val official: String = common,
)

@Serializable
data class CountryDto(
    val name: CountryName,
    val flag: String = "",
    val cca2: String,
)

private var cachedCountries: List<CountryDto>? = null

suspend fun getAllCountries(): List<CountryDto> {
    cachedCountries?.let { return it }
    AppLogger.i("Countries", "getAllCountries")
    try {
        val resp = httpClient.get("$REST_COUNTRIES_BASE/all") {
            parameter("fields", "name,flag,cca2")
        }
        if (!resp.status.isSuccess()) {
            val text = resp.bodyAsText()
            AppLogger.e("Countries", "getAllCountries failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
        val result = resp.body<List<CountryDto>>().sortedBy { it.name.common }
        cachedCountries = result
        return result
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Countries", "getAllCountries error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
