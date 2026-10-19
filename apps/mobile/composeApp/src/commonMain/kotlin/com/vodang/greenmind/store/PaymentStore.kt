package com.vodang.greenmind.store

import com.vodang.greenmind.api.payment.CreateCheckoutData
import com.vodang.greenmind.api.payment.CreateCheckoutRequest
import com.vodang.greenmind.api.payment.WasteBillDto
import com.vodang.greenmind.api.payment.WasteCheckoutData
import com.vodang.greenmind.api.payment.WasteCheckoutRequest
import com.vodang.greenmind.api.payment.createCheckout
import com.vodang.greenmind.api.payment.createWasteCheckout
import com.vodang.greenmind.api.payment.getWasteBills
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object PaymentStore {

    const val FAKE_BILL_PREFIX = "FAKE-DEMO-"

    fun isFakeBill(bill: WasteBillDto): Boolean = bill.householdId.startsWith(FAKE_BILL_PREFIX)

    private var fakeCounter = 0

    private fun nextFakeBill(): WasteBillDto {
        fakeCounter += 1
        return WasteBillDto(
            householdId = "$FAKE_BILL_PREFIX$fakeCounter",
            year = 2026,
            month = 6,
            billName = "Hoá đơn thu gom rác tháng 06/2026",
            total = 151321.0,
            ratePerKg = 5000,
            dueDate = "2026-06-25",
            isPaid = false,
            recordCount = 12,
            paidCount = 0,
            lastPickedAt = "2026-06-20T10:00:00Z",
        )
    }

    data class PaymentState(
        val bills: List<WasteBillDto> = emptyList(),
        val isLoading: Boolean = false,
        val isCheckoutLoading: Boolean = false,
        val error: String? = null,
        val pendingFakeCheckoutId: String? = null,
    )

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    suspend fun fetchBills(paid: Boolean? = null) {
        val token = SettingsStore.getAccessToken()
        if (token == null) {
            _state.update { it.copy(error = "Not logged in") }
            return
        }
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            val response = getWasteBills(token, paid = paid)
            _state.update {
                val existingFakes = it.bills.filter { b -> isFakeBill(b) }
                val merged = existingFakes + response.data.filter { b -> !isFakeBill(b) }
                it.copy(bills = merged, isLoading = false)
            }
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "fetchBills error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    fun injectFakeBill() {
        _state.update {
            it.copy(bills = listOf(nextFakeBill()) + it.bills)
        }
    }

    fun markFakeBillPaid(householdId: String) {
        _state.update {
            it.copy(
                bills = it.bills.map { b -> if (b.householdId == householdId) b.copy(isPaid = true) else b },
                pendingFakeCheckoutId = if (it.pendingFakeCheckoutId == householdId) null else it.pendingFakeCheckoutId,
            )
        }
    }

    /** Mark the fake bill that initiated the most recent Stripe checkout as paid. */
    fun markPendingFakeBillPaid() {
        val id = _state.value.pendingFakeCheckoutId ?: return
        markFakeBillPaid(id)
    }

    suspend fun createCheckoutForBill(bill: WasteBillDto): Result<WasteCheckoutData> {
        val token = SettingsStore.getAccessToken()
            ?: return Result.failure(IllegalStateException("Not logged in"))
        _state.update { it.copy(isCheckoutLoading = true, error = null) }
        return try {
            val response = createWasteCheckout(
                token,
                WasteCheckoutRequest(
                    successUrl = "greenmind://payment/success",
                    cancelUrl = "greenmind://payment/cancel",
                    householdId = bill.householdId,
                    month = bill.month.toString(),
                    year = bill.year.toString(),
                ),
            )
            _state.update { it.copy(isCheckoutLoading = false) }
            Result.success(response.data)
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "createCheckout error: ${e.message}")
            _state.update { it.copy(error = e.message, isCheckoutLoading = false) }
            Result.failure(e)
        }
    }

    suspend fun createCheckoutForFakeBill(bill: WasteBillDto): Result<CreateCheckoutData> {
        val token = SettingsStore.getAccessToken()
            ?: return Result.failure(IllegalStateException("Not logged in"))
        _state.update { it.copy(isCheckoutLoading = true, error = null, pendingFakeCheckoutId = bill.householdId) }
        return try {
            val response = createCheckout(
                token,
                CreateCheckoutRequest(
                    amount = bill.total,
                    currency = "vnd",
                    description = bill.billName,
                    successUrl = "greenmind://payment/success",
                    cancelUrl = "greenmind://payment/cancel",
                ),
            )
            _state.update { it.copy(isCheckoutLoading = false) }
            Result.success(response.data)
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "createCheckout(fake) error: ${e.message}")
            _state.update { it.copy(error = e.message, isCheckoutLoading = false, pendingFakeCheckoutId = null) }
            Result.failure(e)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
