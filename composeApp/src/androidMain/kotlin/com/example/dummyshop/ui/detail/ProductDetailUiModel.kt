package com.example.dummyshop.ui.detail

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.domain.model.ProductDetail

@Immutable
data class ProductDetailUiState(
    val screen: Screen = Screen.Loading,
    val isTogglingFavorite: Boolean = false,
) {
    @Immutable
    sealed interface Screen {
        data object Loading : Screen

        data class Error(
            val kind: ProductDetailErrorKind,
            val canRetry: Boolean,
        ) : Screen

        data class Content(
            val product: ProductDetail,
            val refresh: RefreshState,
        ) : Screen
    }
}

@Immutable
sealed interface RefreshState {
    data object Idle : RefreshState
    data object InFlight : RefreshState
    data class Failed(val error: AppError) : RefreshState
}

enum class ProductDetailErrorKind { Offline, Generic }

sealed interface ProductDetailEvent {
    data object Retry : ProductDetailEvent
    data object ToggleFavorite : ProductDetailEvent
    data object BackClicked : ProductDetailEvent
}

sealed interface ProductDetailEffect {
    data object NavigateBack : ProductDetailEffect
    data class ShowMessage(@StringRes val messageResId: Int) : ProductDetailEffect
}

internal sealed interface InternalIntent {
    data class ProductUpdated(val product: ProductDetail?) : InternalIntent
    data class RefreshFinished(val error: AppError?) : InternalIntent
    data class ToggleFavoriteFinished(val error: AppError?) : InternalIntent
}

fun AppError.toDetailKind(): ProductDetailErrorKind = when (this) {
    is AppError.Network.NoInternet -> ProductDetailErrorKind.Offline
    else -> ProductDetailErrorKind.Generic
}
