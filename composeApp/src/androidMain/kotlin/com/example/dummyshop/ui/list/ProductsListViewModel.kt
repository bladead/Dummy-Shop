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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsListViewModel(
    private val observeProductsUseCase: ObserveProductsUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val refreshProductsUseCase: RefreshProductsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private data class State(
        val query: String = "",
        val allProducts: List<ProductSummary> = emptyList(),
        val syncStatus: SyncStatus? = null,
        val isRefreshing: Boolean = false,
        val lastRefreshError: AppError? = null,
        val hasLoadedOnce: Boolean = false,
        val togglingIds: Set<Long> = emptySet(),
        val started: Boolean = false,
    )

    private val state = MutableStateFlow(State())

    private val _uiState = MutableStateFlow(ProductsListUiState())
    val uiState: StateFlow<ProductsListUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProductsListEffect>(extraBufferCapacity = 64)
    val effects = _effects.asSharedFlow()

    fun bind() {
        val s = state.value
        if (s.started) return

        state.update { it.copy(started = true) }
        render()

        viewModelScope.launch {
            observeProductsUseCase().collect { products ->
                onInternal(InternalIntent.ProductsUpdated(products))
            }
        }

        viewModelScope.launch {
            observeSyncStatusUseCase().collect { status ->
                onInternal(InternalIntent.SyncStatusUpdated(status))
            }
        }

        refresh()
    }

    fun onEvent(event: ProductsListEvent) {
        when (event) {
            ProductsListEvent.Retry -> refresh()

            is ProductsListEvent.QueryChanged -> {
                state.update { it.copy(query = event.value) }
                render()
            }

            is ProductsListEvent.OpenProduct -> {
                viewModelScope.launch {
                    _effects.emit(ProductsListEffect.NavigateToDetail(event.id))
                }
            }

            is ProductsListEvent.ToggleFavorite -> toggleFavorite(event.id)
        }
    }

    private fun onInternal(intent: InternalIntent) {
        when (intent) {
            is InternalIntent.ProductsUpdated -> {
                state.update {
                    it.copy(
                        allProducts = intent.products,
                        hasLoadedOnce = true
                    )
                }
                render()
            }

            is InternalIntent.SyncStatusUpdated -> {
                state.update { it.copy(syncStatus = intent.status) }
                render()
            }

            is InternalIntent.RefreshFinished -> {
                state.update {
                    it.copy(
                        isRefreshing = false,
                        lastRefreshError = intent.error
                    )
                }
                render()
            }

            is InternalIntent.ToggleFavoriteFinished -> {
                state.update { it.copy(togglingIds = it.togglingIds - intent.id) }
                render()

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

    private fun refresh() {
        val s = state.value
        if (s.isRefreshing) return

        state.update { it.copy(isRefreshing = true, lastRefreshError = null) }
        render()

        viewModelScope.launch {
            val result = refreshProductsUseCase()
            val error = (result as? AppResult.Failure)?.error
            onInternal(InternalIntent.RefreshFinished(error))
        }
    }

    private fun toggleFavorite(productId: Long) {
        val s = state.value
        if (s.togglingIds.contains(productId)) return

        state.update { it.copy(togglingIds = it.togglingIds + productId) }
        render()

        viewModelScope.launch {
            val result = toggleFavoriteUseCase(productId)
            val error = (result as? AppResult.Failure)?.error
            onInternal(InternalIntent.ToggleFavoriteFinished(productId, error))
        }
    }

    private fun render() {
        _uiState.value = state.value.toUiState()
    }

    private fun State.toUiState(): ProductsListUiState {
        val filteredProducts = allProducts.filterByQuery(query)
        val canRetry = !isRefreshing

        val hasCache = allProducts.isNotEmpty()
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

            hasLoadedOnce && filteredProducts.isEmpty() ->
                ProductsListUiState.Screen.Empty(canRetry = canRetry)

            else ->
                ProductsListUiState.Screen.Content(
                    items = filteredProducts,
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
