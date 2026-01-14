package com.example.dummyshop.ui.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.domain.model.SyncStatus

@Immutable
data class ProductsListUiState(
    val query: String = "",
    val screen: Screen = Screen.Loading,
) {
    @Immutable
    sealed interface Screen {
        data object Loading : Screen

        data class Error(
            val kind: ProductsListErrorKind,
            val canRetry: Boolean,
        ) : Screen

        data class Empty(
            val canRetry: Boolean,
        ) : Screen

        data class Content(
            val items: List<ProductSummary>,
            val togglingIds: Set<Long>,
            val banner: ProductsListBanner?,
        ) : Screen
    }
}

@Immutable
sealed interface ProductsListBanner {
    data class OfflineCached(val canRetry: Boolean) : ProductsListBanner
    data class StaleCached(val canRetry: Boolean) : ProductsListBanner
}

enum class ProductsListErrorKind { Offline, Generic }

sealed interface ProductsListEvent {
    data object Retry : ProductsListEvent
    data class QueryChanged(val value: String) : ProductsListEvent
    data class OpenProduct(val id: Long) : ProductsListEvent
    data class ToggleFavorite(val id: Long) : ProductsListEvent
}

sealed interface ProductsListEffect {
    data class NavigateToDetail(val id: Long) : ProductsListEffect
    data class ShowSnackbar(@StringRes val messageRes: Int) : ProductsListEffect
}

internal sealed interface InternalIntent {
    data class ProductsUpdated(val products: List<ProductSummary>) : InternalIntent
    data class SyncStatusUpdated(val status: SyncStatus) : InternalIntent
    data class RefreshFinished(val error: AppError?) : InternalIntent
    data class ToggleFavoriteFinished(val id: Long, val error: AppError?) : InternalIntent
}

internal fun AppError.toUiKind(): ProductsListErrorKind = when (this) {
    is AppError.Network.NoInternet -> ProductsListErrorKind.Offline
    else -> ProductsListErrorKind.Generic
}

internal fun List<ProductSummary>.filterByQuery(query: String): List<ProductSummary> {
    if (query.isBlank()) return this
    val q = query.trim()
    return filter { it.title.contains(q, ignoreCase = true) }
}
