package com.example.dummyshop.ui.list

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.domain.model.ProductSummary

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

sealed interface ProductsListIntent {
    data object ScreenStarted : ProductsListIntent
    data class QueryChanged(val value: String) : ProductsListIntent
    data object Retry : ProductsListIntent
    data class OpenProduct(val id: Long) : ProductsListIntent
    data class ToggleFavorite(val id: Long) : ProductsListIntent

    data class ProductsUpdated(val products: List<ProductSummary>) : ProductsListIntent
    data class SyncStatusUpdated(val status: com.dummyshop.shared.domain.model.SyncStatus) :
        ProductsListIntent

    data class RefreshFinished(val error: AppError?) : ProductsListIntent
    data class ToggleFavoriteFinished(val id: Long, val error: AppError?) : ProductsListIntent
}

sealed interface ProductsListEffect {
    data class NavigateToDetail(val id: Long) : ProductsListEffect
    data class ShowSnackbar(@StringRes val messageRes: Int) : ProductsListEffect
}

fun AppError.toUiKind(): ProductsListErrorKind = when (this) {
    is AppError.Network.NoInternet -> ProductsListErrorKind.Offline
    else -> ProductsListErrorKind.Generic
}

internal fun List<ProductSummary>.filterByQuery(query: String): List<ProductSummary> {
    if (query.isBlank()) return this
    val q = query.trim()
    return filter { it.title.contains(q, ignoreCase = true) }
}
