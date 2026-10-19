package com.vodang.greenmind.accounts

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSUserDefaults

private const val KEY = "greenmind_accounts_v1"

actual object AccountsRepository {
    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    actual val accountsFlow: StateFlow<List<Account>> get() = _accounts

    actual fun initialize(platformContext: Any?) {
        val defaults = NSUserDefaults.standardUserDefaults()
        val raw = defaults.stringForKey(KEY)
        if (!raw.isNullOrEmpty()) {
            val items = raw.split("\u0001").filter { it.isNotEmpty() }
            val list = items.mapNotNull { entry ->
                val parts = entry.split("\u0002")
                if (parts.size >= 2) Account(parts[0], parts[1]) else null
            }
            _accounts.value = list
        }
    }

    private fun persist(list: List<Account>) {
        val joined = list.joinToString(separator = "\u0001") { "${it.email}\u0002${it.password}" }
        val defaults = NSUserDefaults.standardUserDefaults()
        defaults.setObject(joined, KEY)
        // no-op: NSUserDefaults synchronizes in the background
    }

    actual suspend fun addAccount(account: Account) {
        val list = _accounts.value.toMutableList()
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
