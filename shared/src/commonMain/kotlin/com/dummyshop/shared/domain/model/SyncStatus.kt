package com.dummyshop.shared.domain.model

sealed interface SyncStatus {
    data class UpToDate(val lastSuccessAtMs: Long) : SyncStatus
    data class Stale(
        val lastSuccessAtMs: Long?,
        val lastFailureAtMs: Long
    ) : SyncStatus
    data object Unknown : SyncStatus
}
