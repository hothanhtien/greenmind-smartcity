package com.vodang.greenmind.accounts

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File

actual object AccountsRepository {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val storageFile: File = File(
        System.getProperty("user.home"),
        ".greenmind/accounts.json"
    )

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    actual val accountsFlow: StateFlow<List<Account>> get() = _accounts

    actual fun initialize(platformContext: Any?) {
        scope.launch {
            _accounts.value = load()
        }
    }

    private fun load(): List<Account> {
        if (!storageFile.exists()) return emptyList()
        return runCatching {
            storageFile.readText()
                .trim()
                .removePrefix("[").removeSuffix("]")
                .split("},{")
                .filter { it.isNotBlank() }
                .mapNotNull { chunk ->
                    val raw = chunk.replace("{", "").replace("}", "")
                    val map = raw.split(",").associate { pair ->
                        val (k, v) = pair.split(":", limit = 2)
                        k.trim().removeSurrounding("\"") to v.trim().removeSurrounding("\"")
                    }
                    val email = map["email"] ?: return@mapNotNull null
                    val password = map["password"] ?: ""
                    Account(email, password)
                }
        }.getOrElse { emptyList() }
    }

    private fun persist(list: List<Account>) {
        scope.launch {
            runCatching {
                storageFile.parentFile?.mkdirs()
                val json = list.joinToString(",", "[", "]") {
                    """{"email":"${it.email}","password":"${it.password}"}"""
                }
                storageFile.writeText(json)
            }
        }
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
