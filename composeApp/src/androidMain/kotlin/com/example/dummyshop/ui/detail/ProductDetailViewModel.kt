package com.example.dummyshop.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dummyshop.shared.core.result.AppResult
import com.dummyshop.shared.domain.usecase.ObserveProductDetailUseCase
import com.dummyshop.shared.domain.usecase.RefreshProductUseCase
import com.dummyshop.shared.domain.usecase.ToggleFavoriteUseCase
import com.example.dummyshop.R
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ProductDetailViewModel(
    private val observeProductDetailUseCase: ObserveProductDetailUseCase,
    private val refreshProductUseCase: RefreshProductUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<ProductDetailEffect>()
    val effects = _effects.asSharedFlow()

    private val productIdFlow = MutableStateFlow<Long?>(null)
    private var currentProductId: Long? = null

    init {
        viewModelScope.launch {
            productIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { id -> observeProductDetailUseCase(id) }
                .collect { product ->
                    onInternal(InternalIntent.ProductUpdated(product))
                }
        }
    }

    fun bind(productId: Long) {
        if (currentProductId == productId) return
        currentProductId = productId

        _uiState.value = ProductDetailUiState(
            screen = ProductDetailUiState.Screen.Loading,
            isTogglingFavorite = false
        )

        productIdFlow.value = productId
        refresh(productId)
    }

    fun onEvent(event: ProductDetailEvent) {
        when (event) {
            ProductDetailEvent.BackClicked -> {
                viewModelScope.launch { _effects.emit(ProductDetailEffect.NavigateBack) }
            }

            ProductDetailEvent.Retry -> {
                val id = currentProductId ?: return
                refresh(id)
            }

            ProductDetailEvent.ToggleFavorite -> {
                val id = currentProductId ?: return
                toggleFavorite(id)
            }
        }
    }

    private fun onInternal(intent: InternalIntent) {
        when (intent) {
            is InternalIntent.ProductUpdated -> {
                val product = intent.product ?: return

                _uiState.value = _uiState.value.copy(
                    screen = ProductDetailUiState.Screen.Content(
                        product = product,
                        refresh = (
                                _uiState.value.screen as? ProductDetailUiState.Screen.Content
                                )?.refresh
                            ?: RefreshState.Idle
                    )
                )
            }

            is InternalIntent.RefreshFinished -> {
                val current = _uiState.value
                val screen = current.screen

                _uiState.value = when (screen) {
                    is ProductDetailUiState.Screen.Content -> {
                        val refresh =
                            intent.error?.let { RefreshState.Failed(it) } ?: RefreshState.Idle
                        current.copy(screen = screen.copy(refresh = refresh))
                    }

                    is ProductDetailUiState.Screen.Loading,
                    is ProductDetailUiState.Screen.Error,
                        -> {
                        val error = intent.error
                        if (error == null) current
                        else current.copy(
                            screen = ProductDetailUiState.Screen.Error(
                                kind = error.toDetailKind(),
                                canRetry = true
                            )
                        )
                    }
                }
            }

            is InternalIntent.ToggleFavoriteFinished -> {
                _uiState.value = _uiState.value.copy(isTogglingFavorite = false)

                if (intent.error != null) {
                    viewModelScope.launch {
                        _effects.emit(
                            ProductDetailEffect.ShowMessage(
                                R.string.action_failed
                            )
                        )
                    }
                }
            }
        }
    }

    private fun refresh(productId: Long) {
        val current = _uiState.value
        val screen = current.screen

        if (screen is ProductDetailUiState.Screen.Content
            && screen.refresh is RefreshState.InFlight
        ) return

        _uiState.value = when (screen) {
            is ProductDetailUiState.Screen.Content ->
                current.copy(screen = screen.copy(refresh = RefreshState.InFlight))

            is ProductDetailUiState.Screen.Error ->
                current.copy(screen = ProductDetailUiState.Screen.Loading)

            is ProductDetailUiState.Screen.Loading ->
                current
        }

        viewModelScope.launch {
            val result = refreshProductUseCase(productId)
            if (currentProductId != productId) return@launch

            val error = (result as? AppResult.Failure)?.error
            onInternal(InternalIntent.RefreshFinished(error))
        }
    }

    private fun toggleFavorite(productId: Long) {
        val current = _uiState.value
        if (current.isTogglingFavorite) return

        _uiState.value = current.copy(isTogglingFavorite = true)

        viewModelScope.launch {
            val result = toggleFavoriteUseCase(productId)
            if (currentProductId != productId) return@launch

            val error = (result as? AppResult.Failure)?.error
            onInternal(InternalIntent.ToggleFavoriteFinished(error))
        }
    }
}
