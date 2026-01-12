package com.example.dummyshop.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dummyshop.shared.domain.model.ProductSummary
import com.example.dummyshop.R
import com.example.dummyshop.ui.format.formatAsPrice
import com.example.dummyshop.ui.theme.DummyShopTheme

@Composable
fun ProductCard(
    item: ProductSummary,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation()
    ) {
        Column(modifier = Modifier.padding(DummyShopTheme.spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = DummyShopTheme.spacing.md)
                )

                Text(
                    text = item.price.formatAsPrice(),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DummyShopTheme.spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(item.category) }
                )

                val icon =
                    if (item.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                val cd =
                    if (item.isFavorite) R.string.cd_remove_favorite else R.string.cd_add_favorite

                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = icon,
                        contentDescription = stringResource(cd)
                    )
                }
            }
        }
    }
}
