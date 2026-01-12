package com.example.dummyshop.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dummyshop.shared.core.result.AppError
import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.domain.model.ProductSummary
import com.dummyshop.shared.domain.model.SyncStatus
import com.dummyshop.shared.domain.usecase.ObserveProductsUseCase
import com.dummyshop.shared.domain.usecase.ObserveSyncStatusUseCase
import com.dummyshop.shared.domain.usecase.RefreshProductsUseCase
import com.dummyshop.shared.domain.usecase.ToggleFavoriteUseCase
import com.example.dummyshop.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted

class ProductsListViewModel(
    private val observeProductsUseCase: ObserveProductsUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val refreshProductsUseCase: RefreshProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private data class State(
        val query: String = "",
        val products: List<ProductSummary> = emptyList(),
        val syncStatus: SyncStatus? = null,
        val isRefreshing: Boolean = false,
        val lastRefreshError: AppError? = null,
        val hasLoadedOnce: Boolean = false,
        val togglingIds: Set<Long> = emptySet(),
        val started: Boolean = false,
    )

    private val state = MutableStateFlow(State())

    val uiState: StateFlow<ProductsListUiState> =
        state
            .map { it.toUiState() }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ProductsListUiState()
            )

    private val _effects = MutableSharedFlow<ProductsListEffect>(extraBufferCapacity = 64)
    val effects = _effects.asSharedFlow()

    fun onIntent(intent: ProductsListIntent) {
        when (intent) {
            ProductsListIntent.ScreenStarted -> startIfNeeded()

            is ProductsListIntent.QueryChanged ->
                state.update { it.copy(query = intent.value) }

            ProductsListIntent.Retry ->
                refresh()

            is ProductsListIntent.OpenProduct ->
                viewModelScope.launch {
                    _effects.emit(ProductsListEffect.NavigateToDetail(intent.id))
                }

            is ProductsListIntent.ToggleFavorite ->
                toggleFavorite(intent.id)

            is ProductsListIntent.ProductsUpdated ->
                state.update { it.copy(products = intent.products, hasLoadedOnce = true) }

            is ProductsListIntent.SyncStatusUpdated ->
                state.update { it.copy(syncStatus = intent.status) }

            is ProductsListIntent.RefreshFinished ->
                state.update {
                    it.copy(
                        isRefreshing = false,
                        lastRefreshError = intent.error
                    )
                }

            is ProductsListIntent.ToggleFavoriteFinished -> {
                state.update { it.copy(togglingIds = it.togglingIds - intent.id) }
                if (intent.error != null) {
                    viewModelScope.launch {
                        _effects.emit(
                            ProductsListEffect.ShowSnackbar(
                                R.string.snackbar_favorite_failed
                            )
                        )
                    }
                }
            }
        }
    }

    private fun startIfNeeded() {
        val current = state.value
        if (current.started) return

        state.update { it.copy(started = true) }

        viewModelScope.launch {
            observeProductsUseCase().collect { products ->
                onIntent(ProductsListIntent.ProductsUpdated(products))
            }
        }

        viewModelScope.launch {
            observeSyncStatusUseCase().collect { status ->
                onIntent(ProductsListIntent.SyncStatusUpdated(status))
            }
        }

        refresh()
    }

    private fun refresh() {
        val s = state.value
        if (s.isRefreshing) return

        state.update { it.copy(isRefreshing = true, lastRefreshError = null) }

        viewModelScope.launch {
            val result = refreshProductsUseCase()
            val error = (result as? AppResult.Failure)?.error
            onIntent(ProductsListIntent.RefreshFinished(error))
        }
    }

    private fun toggleFavorite(productId: Long) {
        val s = state.value
        if (s.togglingIds.contains(productId)) return

        state.update { it.copy(togglingIds = it.togglingIds + productId) }

        viewModelScope.launch {
            val result = toggleFavoriteUseCase(productId)
            val error = (result as? AppResult.Failure)?.error
            onIntent(ProductsListIntent.ToggleFavoriteFinished(productId, error))
        }
    }

    private fun State.toUiState(): ProductsListUiState {
        val filtered = products.filterByQuery(query)
        val canRetry = !isRefreshing

        val hasCache = products.isNotEmpty()
        val banner: ProductsListBanner? = when {
            hasCache && lastRefreshError is AppError.Network.NoInternet ->
                ProductsListBanner.OfflineCached(canRetry)

            hasCache && syncStatus is SyncStatus.Stale ->
                ProductsListBanner.StaleCached(canRetry)

            else -> null
        }

        val screen: ProductsListUiState.Screen = when {
            !hasLoadedOnce && isRefreshing ->
                ProductsListUiState.Screen.Loading

            !hasLoadedOnce && lastRefreshError != null ->
                ProductsListUiState.Screen.Error(
                    kind = lastRefreshError.toUiKind(),
                    canRetry = canRetry
                )

            hasLoadedOnce && filtered.isEmpty() ->
                ProductsListUiState.Screen.Empty(canRetry = canRetry)

            else ->
                ProductsListUiState.Screen.Content(
                    items = filtered,
                    togglingIds = togglingIds,
                    banner = banner
                )
        }

        return ProductsListUiState(
            query = query,
            screen = screen
        )
    }
}
