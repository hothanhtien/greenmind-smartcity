package com.vodang.greenmind.accounts

import kotlinx.coroutines.flow.StateFlow

data class Account(val email: String, val password: String)

/**
 * Simple cross-platform accounts repository.
 * Call [initialize] from platform code with a context if needed.
 */
expect object AccountsRepository {
    val accountsFlow: StateFlow<List<Account>>
    fun initialize(platformContext: Any?)
    suspend fun addAccount(account: Account)
    suspend fun removeAccount(account: Account)
}
