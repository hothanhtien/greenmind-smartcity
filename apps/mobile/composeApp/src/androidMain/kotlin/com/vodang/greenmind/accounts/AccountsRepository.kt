package com.vodang.greenmind.accounts

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray

actual object AccountsRepository {
    private const val PREFS = "greenmind_accounts"
    private const val KEY = "accounts_json"

    private lateinit var ctx: Context
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    actual val accountsFlow: StateFlow<List<Account>> get() = _accounts

    actual fun initialize(platformContext: Any?) {
        ctx = (platformContext as? Context) ?: throw IllegalStateException("Context required")
        // load
        val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, null)
        if (!raw.isNullOrEmpty()) {
            try {
                val arr = JSONArray(raw)
                val list = mutableListOf<Account>()
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val email = obj.optString("email")
                    val password = obj.optString("password")
                    if (email.isNotEmpty()) list.add(Account(email, password))
                }
                _accounts.value = list
            } catch (_: Exception) { /* ignore */ }
        }
    }

    private fun persist(list: List<Account>) {
        scope.launch {
            try {
                val arr = JSONArray()
                for (a in list) {
                    val obj = org.json.JSONObject()
                    obj.put("email", a.email)
                    obj.put("password", a.password)
                    arr.put(obj)
                }
                val prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                prefs.edit().putString(KEY, arr.toString()).apply()
            } catch (_: Exception) {}
        }
    }

    actual suspend fun addAccount(account: Account) {
        val list = _accounts.value.toMutableList()
        // avoid duplicates by email
        list.removeAll { it.email.equals(account.email, ignoreCase = true) }
        list.add(0, account)
        _accounts.value = list
        persist(list)
    }

    actual suspend fun removeAccount(account: Account) {
        val list = _accounts.value.toMutableList()
        list.removeAll { it.email.equals(account.email, ignoreCase = true) }
        _accounts.value = list
        persist(list)
    }
}
