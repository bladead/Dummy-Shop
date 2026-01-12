package com.example.dummyshop.ui.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.dummyshop.R
import com.example.dummyshop.ui.components.ErrorState
import com.example.dummyshop.ui.components.LoadingState
import com.example.dummyshop.ui.format.formatAsPrice
import com.example.dummyshop.ui.theme.DummyShopTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    state: ProductDetailUiState,
    onEvent: (ProductDetailEvent) -> Unit,
    snackbarHost: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(ProductDetailEvent.BackClicked) }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_back)
                        )
                    }
                },
                actions = {
                    val screen = state.screen
                    if (screen is ProductDetailUiState.Screen.Content) {
                        val icon =
                            if (screen.product.isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Outlined.FavoriteBorder
                            }
                        val cd = if (screen.product.isFavorite) {
                            R.string.cd_remove_favorite
                        } else {
                            R.string.cd_add_favorite
                        }

                        IconButton(
                            onClick = { onEvent(ProductDetailEvent.ToggleFavorite) },
                            enabled = !state.isTogglingFavorite
                        ) {
                            Icon(imageVector = icon, contentDescription = stringResource(cd))
                        }
                    }
                }
            )
        },
        snackbarHost = snackbarHost
    ) { padding ->
        when (val screen = state.screen) {
            is ProductDetailUiState.Screen.Loading -> {
                LoadingState(contentPadding = padding)
            }

            is ProductDetailUiState.Screen.Error -> {
                ErrorState(
                    text = stringResource(R.string.load_error),
                    actionLabel = stringResource(R.string.retry),
                    onAction = { onEvent(ProductDetailEvent.Retry) },
                    contentPadding = padding
                )
            }

            is ProductDetailUiState.Screen.Content -> {
                val product = screen.product

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(DummyShopTheme.spacing.lg)
                ) {
                    item {
                        Text(
                            text = product.title,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }

                    item {
                        Text(
                            text = stringResource(
                                R.string.product_price_value,
                                product.price.formatAsPrice()
                            ),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = DummyShopTheme.spacing.md)
                        )
                    }

                    item {
                        Text(
                            text = stringResource(
                                R.string.product_category_value,
                                product.category
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = DummyShopTheme.spacing.sm)
                        )
                    }

                    item {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = DummyShopTheme.spacing.lg)
                        )
                    }
                }
            }
        }
    }
}
