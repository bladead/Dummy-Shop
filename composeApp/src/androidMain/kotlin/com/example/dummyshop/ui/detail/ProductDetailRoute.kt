package com.example.dummyshop.ui.detail

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductDetailRoute(
    productId: Long,
    onBack: () -> Unit
) {
    val viewModel: ProductDetailViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(productId) {
        viewModel.bind(productId)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                ProductDetailEffect.NavigateBack -> onBack()
                is ProductDetailEffect.ShowMessage ->
                    snackbarHostState.showSnackbar(context.getString(effect.messageResId))
            }
        }
    }

    ProductDetailScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}
