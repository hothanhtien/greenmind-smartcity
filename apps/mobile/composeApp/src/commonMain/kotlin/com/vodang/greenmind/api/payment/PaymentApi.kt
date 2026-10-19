package com.vodang.greenmind.api.payment

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class WasteBillDto(
    val householdId: String,
    val year: Int,
    val month: Int,
    val billName: String,
    val total: Double,
    val ratePerKg: Int,
    val dueDate: String,
    val isPaid: Boolean,
    val recordCount: Int,
    val paidCount: Int,
    val lastPickedAt: String,
)

@Serializable
data class WasteBillsResponse(
    val message: String,
    val data: List<WasteBillDto>,
)

@Serializable
data class WasteCheckoutRequest(
    val successUrl: String,
    val cancelUrl: String,
    val householdId: String,
    val month: String,
    val year: String,
)

@Serializable
data class WasteCheckoutData(
    val url: String,
    val sessionId: String,
    val billName: String,
    val totalAmount: Double,
    val totalMassKg: Double,
    val ratePerKg: Int,
    val recordCount: Int,
    val month: Int,
    val year: Int,
)

@Serializable
data class WasteCheckoutResponse(
    val message: String,
    val data: WasteCheckoutData,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /payments/waste-bills?paid=&page=&limit= */
suspend fun getWasteBills(
    accessToken: String,
    paid: Boolean? = null,
    page: Int = 1,
    limit: Int = 20,
): WasteBillsResponse {
    AppLogger.i("Payment", "getWasteBills paid=$paid page=$page limit=$limit")
    return try {
        val resp = httpClient.get("$BASE_URL/payments/waste-bills") {
            header("Authorization", "Bearer $accessToken")
            if (paid != null) parameter("paid", paid.toString())
            parameter("page", page.toString())
            parameter("limit", limit.toString())
        }
        if (resp.status.isSuccess()) resp.body()
        else throw ApiException(resp.status.value, resp.bodyAsText())
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}

@Serializable
data class CreateCheckoutRequest(
    val amount: Double,
    val currency: String,
    val description: String,
    val successUrl: String,
    val cancelUrl: String,
)

@Serializable
data class CreateCheckoutData(
    val url: String,
    val sessionId: String,
)

@Serializable
data class CreateCheckoutResponse(
    val message: String,
    val data: CreateCheckoutData,
)

/** POST /payments/create-checkout */
suspend fun createCheckout(
    accessToken: String,
    request: CreateCheckoutRequest,
): CreateCheckoutResponse {
    AppLogger.i("Payment", "createCheckout amount=${request.amount} desc=${request.description}")
    return try {
        val resp = httpClient.post("$BASE_URL/payments/create-checkout") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (resp.status.isSuccess()) resp.body()
        else throw ApiException(resp.status.value, resp.bodyAsText())
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /payments/waste-checkout */
suspend fun createWasteCheckout(
    accessToken: String,
    request: WasteCheckoutRequest,
): WasteCheckoutResponse {
    AppLogger.i("Payment", "createWasteCheckout household=${request.householdId} ${request.month}/${request.year}")
    return try {
        val resp = httpClient.post("$BASE_URL/payments/waste-checkout") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        if (resp.status.isSuccess()) resp.body()
        else throw ApiException(resp.status.value, resp.bodyAsText())
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        throw ApiException(0, e.message ?: "Network error")
    }
}
