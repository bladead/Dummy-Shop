package com.example.dummyshop.ui.list

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProductsRoute(
    onOpenProduct: (Long) -> Unit,
) {
    val viewModel: ProductsListViewModel = koinViewModel()
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(ProductsListIntent.ScreenStarted)
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is ProductsListEffect.NavigateToDetail -> onOpenProduct(effect.id)
                is ProductsListEffect.ShowSnackbar ->
                    snackbarHostState.showSnackbar(
                        context.getString(effect.messageRes)
                    )
            }
        }
    }

    ProductsScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    )
}
