package com.renobile.carrinho.features.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.productPreview

@Composable
fun ListOptionsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit = {},
    onEdit: (ProductEntity) -> Unit = {},
    onMoveToCart: (ProductEntity) -> Unit = {},
    onChangeQuantity: (ProductEntity, Double) -> Unit = { _, _ -> },
    onDelete: (ProductEntity) -> Unit = {}
) {
    val options = stringArrayResource(R.array.product_list_options)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(product.name) },
        text = {
            Column {
                ListItem(
                    headlineContent = { Text(options.getOrNull(0) ?: stringResource(R.string.edit_product)) },
                    modifier = Modifier.clickable { onEdit(product) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(1) ?: "+ 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, 1.0) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(2) ?: "- 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, -1.0) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(3) ?: "Excluir") },
                    modifier = Modifier.clickable { onDelete(product) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(4) ?: stringResource(R.string.move_to_cart)) },
                    modifier = Modifier.clickable { onMoveToCart(product) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        },
        confirmButton = {}
    )
}

@Preview
@Composable
private fun ListOptionsDialogPreview() {
    MaterialTheme {
        ListOptionsDialog(
            product = productPreview
        )
    }
}
