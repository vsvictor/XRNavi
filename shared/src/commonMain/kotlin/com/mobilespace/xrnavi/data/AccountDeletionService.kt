package com.mobilespace.xrnavi.data

import com.mobilespace.xrnavi.domain.AccountDeletionRepository
import com.mobilespace.xrnavi.domain.AccountDeletionResult

class UnconfiguredAccountDeletionService : AccountDeletionRepository {
    override suspend fun requestDeletion(): AccountDeletionResult = AccountDeletionResult.NotConfigured
    override suspend fun exportData(): AccountDeletionResult = AccountDeletionResult.NotConfigured
}
