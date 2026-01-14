package com.example.dummyshop.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.dummyshop.R
import com.example.dummyshop.ui.components.EmptyState
import com.example.dummyshop.ui.components.ErrorCardState
import com.example.dummyshop.ui.components.InfoBanner
import com.example.dummyshop.ui.components.LoadingState
import com.example.dummyshop.ui.theme.DummyShopTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    state: ProductsListUiState,
    onEvent: (ProductsListEvent) -> Unit,
    snackbarHost: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.products_title)) })
        },
        snackbarHost = snackbarHost
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DummyShopTheme.spacing.lg)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { onEvent(ProductsListEvent.QueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DummyShopTheme.spacing.md),
                placeholder = { Text(stringResource(R.string.search_hint)) },
                singleLine = true
            )

            when (val screen = state.screen) {
                is ProductsListUiState.Screen.Loading -> {
                    LoadingState(contentPadding = PaddingValues(top = DummyShopTheme.spacing.xl))
                }

                is ProductsListUiState.Screen.Error -> {
                    val isOffline = screen.kind == ProductsListErrorKind.Offline
                    ErrorCardState(
                        title = stringResource(
                            if (isOffline) {
                                R.string.error_offline_title
                            } else {
                                R.string.error_generic_title
                            }
                        ),
                        message = stringResource(
                            if (isOffline) {
                                R.string.error_offline_message
                            } else {
                                R.string.error_generic_message
                            }
                        ),
                        actionLabel = stringResource(R.string.retry),
                        onAction = { onEvent(ProductsListEvent.Retry) },
                        isOffline = isOffline,
                        contentPadding = PaddingValues(top = DummyShopTheme.spacing.xl)
                    )
                }

                is ProductsListUiState.Screen.Empty -> {
                    EmptyState(
                        text = stringResource(R.string.empty_products),
                        contentPadding = PaddingValues(top = DummyShopTheme.spacing.xl)
                    )
                }

                is ProductsListUiState.Screen.Content -> {
                    screen.banner?.let { banner ->
                        val (textRes, canRetry) = when (banner) {
                            is ProductsListBanner.OfflineCached ->
                                R.string.banner_offline_cached to banner.canRetry

                            is ProductsListBanner.StaleCached ->
                                R.string.banner_stale_cached to banner.canRetry
                        }

                        InfoBanner(
                            text = stringResource(textRes),
                            actionLabel = stringResource(R.string.retry),
                            actionEnabled = canRetry,
                            onAction = { onEvent(ProductsListEvent.Retry) },
                            modifier = Modifier.padding(top = DummyShopTheme.spacing.md)
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = DummyShopTheme.spacing.md),
                        contentPadding = PaddingValues(vertical = DummyShopTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(
                            DummyShopTheme.spacing.md
                        )
                    ) {
                        items(
                            items = screen.items,
                            key = { it.id }
                        ) { item ->
                            ProductCard(
                                item = item,
                                onClick = { onEvent(ProductsListEvent.OpenProduct(item.id)) },
                                onToggleFavorite = { onEvent(ProductsListEvent.ToggleFavorite(item.id)) },
                                modifier = Modifier,
                            )
                        }
                    }
                }
            }
        }
    }
}
