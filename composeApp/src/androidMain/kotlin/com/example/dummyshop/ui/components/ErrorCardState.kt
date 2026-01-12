package com.example.dummyshop.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.dummyshop.ui.theme.DummyShopTheme

@Composable
fun ErrorCardState(
    title: String,
    message: String,
    actionLabel: String,
    onAction: () -> Unit,
    isOffline: Boolean,
    contentPadding: PaddingValues = PaddingValues(),
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(DummyShopTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard {
            Column(modifier = Modifier.padding(DummyShopTheme.spacing.xl)) {
                Icon(
                    imageVector =
                        if (isOffline) Icons.Outlined.CloudOff else Icons.Outlined.ErrorOutline,
                    contentDescription = null
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = DummyShopTheme.spacing.md)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = DummyShopTheme.spacing.sm)
                )
                Button(
                    onClick = onAction,
                    modifier = Modifier.padding(top = DummyShopTheme.spacing.lg)
                ) {
                    Text(actionLabel)
                }
            }
        }
    }
}
