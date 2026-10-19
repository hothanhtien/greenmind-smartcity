package com.vodang.greenmind.api.nominatim

import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

private const val NOMINATIM_BASE = "https://nominatim.openstreetmap.org"
private const val USER_AGENT = "GreenMind/1.0"

// ── Response models ───────────────────────────────────────────────────────────

@Serializable
data class NominatimAddress(
    val house_number: String? = null,
    val road: String? = null,
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val village: String? = null,
    val town: String? = null,
    val city: String? = null,
    val municipality: String? = null,
    val county: String? = null,
    val state: String? = null,
    val region: String? = null,
    val postcode: String? = null,
    val country: String? = null,
    val country_code: String? = null,
)

/** Returned by search, reverse, and lookup endpoints. */
@Serializable
data class NominatimPlace(
    val place_id: Long,
    val licence: String? = null,
    val osm_type: String? = null,
    val osm_id: Long? = null,
    val lat: String,
    val lon: String,
    val place_rank: Int? = null,
    val category: String? = null,
    val type: String? = null,
    val importance: Double? = null,
    val addresstype: String? = null,
    val name: String? = null,
    val display_name: String,
    val address: NominatimAddress? = null,
    val boundingbox: List<String>? = null,
    val namedetails: Map<String, String>? = null,
    val extratags: Map<String, String>? = null,
)

/** Returned by the /details endpoint. Complex/optional fields use JsonElement. */
@Serializable
data class NominatimDetails(
    val place_id: Long,
    val parent_place_id: Long? = null,
    val osm_type: String? = null,
    val osm_id: Long? = null,
    val category: String? = null,
    val type: String? = null,
    val admin_level: String? = null,
    val localname: String? = null,
    val names: Map<String, String>? = null,
    val addresstags: Map<String, String>? = null,
    val housenumber: String? = null,
    val postcode: String? = null,
    val country_code: String? = null,
    val calculated_postcode: String? = null,
    val rank_address: Int? = null,
    val rank_search: Int? = null,
    val geometry: JsonElement? = null,
    val linked_places: JsonElement? = null,
    val address: JsonElement? = null,
    val keywords: JsonElement? = null,
    val hierarchy: JsonElement? = null,
    val group_hierarchy: JsonElement? = null,
)

/** Returned by the /status endpoint. */
@Serializable
data class NominatimStatus(
    val status: Int,
    val message: String,
    val data_updated: String? = null,
    val software_version: String? = null,
    val database_version: String? = null,
)

// ── Option classes ────────────────────────────────────────────────────────────

data class SearchOptions(
    val limit: Int = 10,
    val addressDetails: Boolean = true,
    val nameDetails: Boolean = false,
    val extraTags: Boolean = false,
    /** BCP 47 language tag, e.g. "vi", "en". */
    val language: String? = null,
    /** Comma-separated ISO 3166-1 alpha-2 codes, e.g. "vn,us". */
    val countryCodes: String? = null,
    /** Bounding box as "lon1,lat1,lon2,lat2". */
    val viewbox: String? = null,
    val bounded: Boolean = false,
    /** Filter by layer: "address", "poi", "railway", "natural", "manmade". */
    val layer: String? = null,
    /** Filter by feature type: "country", "state", "city", "settlement". */
    val featureType: String? = null,
)

data class ReverseOptions(
    /** Detail level 0 (country) – 18 (building). Default 18. */
    val zoom: Int = 18,
    val addressDetails: Boolean = true,
    val nameDetails: Boolean = false,
    val extraTags: Boolean = false,
    val language: String? = null,
)

data class LookupOptions(
    val addressDetails: Boolean = true,
    val nameDetails: Boolean = false,
    val extraTags: Boolean = false,
    val language: String? = null,
)

data class DetailsOptions(
    val addressDetails: Boolean = false,
    val keywords: Boolean = false,
    val linkedPlaces: Boolean = true,
    val hierarchy: Boolean = false,
    val groupHierarchy: Boolean = false,
    val polygonGeojson: Boolean = false,
    val language: String? = null,
)

// ── Exception ─────────────────────────────────────────────────────────────────

class NominatimException(val code: Int, override val message: String) : Exception(message)

// ── Internal helpers ──────────────────────────────────────────────────────────

private fun HttpRequestBuilder.nominatimDefaults(language: String?) {
    header("User-Agent", USER_AGENT)
    if (language != null) header("Accept-Language", language)
    parameter("format", "jsonv2")
}

private fun boolParam(value: Boolean): Int = if (value) 1 else 0

private suspend inline fun <reified T> checkResponse(resp: HttpResponse): T =
    if (resp.status.isSuccess()) resp.body()
    else {
        val text = resp.bodyAsText()
        AppLogger.e("Nominatim", "${resp.request.method.value} ${resp.request.url.encodedPath} failed: ${resp.status.value} $text")
        throw NominatimException(resp.status.value, text)
    }

// ── API calls ─────────────────────────────────────────────────────────────────

/**
 * GET /search?q=...
 * Free-form forward geocoding: text query → list of matching places.
 */
suspend fun nominatimSearch(
    query: String,
    options: SearchOptions = SearchOptions(),
): List<NominatimPlace> {
    AppLogger.i("Nominatim", "search q=$query")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/search") {
            nominatimDefaults(options.language)
            parameter("q", query)
            parameter("limit", options.limit)
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("namedetails", boolParam(options.nameDetails))
            parameter("extratags", boolParam(options.extraTags))
            if (!options.countryCodes.isNullOrBlank()) parameter("countrycodes", options.countryCodes)
            if (!options.viewbox.isNullOrBlank()) parameter("viewbox", options.viewbox)
            if (options.bounded) parameter("bounded", 1)
            if (!options.layer.isNullOrBlank()) parameter("layer", options.layer)
            if (!options.featureType.isNullOrBlank()) parameter("featureType", options.featureType)
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimSearch error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /search with structured address parameters.
 * Use instead of a free-form query when individual address components are known.
 */
suspend fun nominatimSearchStructured(
    street: String? = null,
    city: String? = null,
    county: String? = null,
    state: String? = null,
    country: String? = null,
    postalCode: String? = null,
    options: SearchOptions = SearchOptions(),
): List<NominatimPlace> {
    AppLogger.i("Nominatim", "searchStructured city=$city country=$country")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/search") {
            nominatimDefaults(options.language)
            if (!street.isNullOrBlank()) parameter("street", street)
            if (!city.isNullOrBlank()) parameter("city", city)
            if (!county.isNullOrBlank()) parameter("county", county)
            if (!state.isNullOrBlank()) parameter("state", state)
            if (!country.isNullOrBlank()) parameter("country", country)
            if (!postalCode.isNullOrBlank()) parameter("postalcode", postalCode)
            parameter("limit", options.limit)
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("namedetails", boolParam(options.nameDetails))
            parameter("extratags", boolParam(options.extraTags))
            if (!options.countryCodes.isNullOrBlank()) parameter("countrycodes", options.countryCodes)
            if (!options.viewbox.isNullOrBlank()) parameter("viewbox", options.viewbox)
            if (options.bounded) parameter("bounded", 1)
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimSearchStructured error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /reverse?lat=...&lon=...
 * Reverse geocoding: coordinates → address.
 *
 * @param zoom Controls the level of detail in the result (0 = country, 18 = building).
 */
suspend fun nominatimReverse(
    lat: Double,
    lon: Double,
    options: ReverseOptions = ReverseOptions(),
): NominatimPlace {
    AppLogger.i("Nominatim", "reverse lat=$lat lon=$lon")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/reverse") {
            nominatimDefaults(options.language)
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("zoom", options.zoom)
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("namedetails", boolParam(options.nameDetails))
            parameter("extratags", boolParam(options.extraTags))
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimReverse error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /lookup?osm_ids=...
 * Look up details for specific OSM objects by their IDs.
 *
 * @param osmIds OSM IDs prefixed with their type letter:
 *   "N" = node, "W" = way, "R" = relation.
 *   Example: listOf("R146656", "W104393803", "N240109189")
 */
suspend fun nominatimLookup(
    osmIds: List<String>,
    options: LookupOptions = LookupOptions(),
): List<NominatimPlace> {
    AppLogger.i("Nominatim", "lookup osmIds=$osmIds")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/lookup") {
            nominatimDefaults(options.language)
            parameter("osm_ids", osmIds.joinToString(","))
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("namedetails", boolParam(options.nameDetails))
            parameter("extratags", boolParam(options.extraTags))
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimLookup error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /details?place_id=...
 * Detailed information about a place by its Nominatim internal place_id.
 */
suspend fun nominatimDetailsByPlaceId(
    placeId: Long,
    options: DetailsOptions = DetailsOptions(),
): NominatimDetails {
    AppLogger.i("Nominatim", "detailsByPlaceId placeId=$placeId")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/details") {
            header("User-Agent", USER_AGENT)
            if (options.language != null) header("Accept-Language", options.language)
            parameter("format", "json")
            parameter("place_id", placeId)
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("keywords", boolParam(options.keywords))
            parameter("linkedplaces", boolParam(options.linkedPlaces))
            parameter("hierarchy", boolParam(options.hierarchy))
            parameter("group_hierarchy", boolParam(options.groupHierarchy))
            parameter("polygon_geojson", boolParam(options.polygonGeojson))
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimDetailsByPlaceId error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /details?osmtype=...&osmid=...
 * Detailed information about a place by its OSM type and numeric ID.
 *
 * @param osmType Single letter: "N" (node), "W" (way), "R" (relation).
 * @param osmId   Numeric OSM ID.
 */
suspend fun nominatimDetailsByOsmId(
    osmType: String,
    osmId: Long,
    options: DetailsOptions = DetailsOptions(),
): NominatimDetails {
    AppLogger.i("Nominatim", "detailsByOsmId type=$osmType id=$osmId")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/details") {
            header("User-Agent", USER_AGENT)
            if (options.language != null) header("Accept-Language", options.language)
            parameter("format", "json")
            parameter("osmtype", osmType.uppercase().take(1))
            parameter("osmid", osmId)
            parameter("addressdetails", boolParam(options.addressDetails))
            parameter("keywords", boolParam(options.keywords))
            parameter("linkedplaces", boolParam(options.linkedPlaces))
            parameter("hierarchy", boolParam(options.hierarchy))
            parameter("group_hierarchy", boolParam(options.groupHierarchy))
            parameter("polygon_geojson", boolParam(options.polygonGeojson))
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimDetailsByOsmId error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}

/**
 * GET /status
 * Returns the operational status of the Nominatim server.
 */
suspend fun nominatimStatus(): NominatimStatus {
    AppLogger.i("Nominatim", "status")
    try {
        val resp = httpClient.get("$NOMINATIM_BASE/status") {
            header("User-Agent", USER_AGENT)
            parameter("format", "json")
        }
        return checkResponse(resp)
    } catch (e: NominatimException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("Nominatim", "nominatimStatus error: ${e.message}")
        throw NominatimException(0, e.message ?: "Network error")
    }
}
