package com.renobile.carrinho.features.list.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity

@Composable
fun ProductListOptionsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onEdit: (ProductEntity) -> Unit,
    onMoveToCart: (ProductEntity) -> Unit,
    onChangeQuantity: (ProductEntity, Double) -> Unit,
    onDelete: (ProductEntity) -> Unit,
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
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(1) ?: "+ 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, 1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(2) ?: "- 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, -1.0) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(3) ?: "Excluir") },
                    modifier = Modifier.clickable { onDelete(product) },
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(4) ?: stringResource(R.string.move_to_cart)) },
                    modifier = Modifier.clickable { onMoveToCart(product) },
                )
            }
        },
        confirmButton = {},
    )
}

@Preview
@Composable
fun ProductListOptionsDialogPreview() {
    MaterialTheme {
        ProductListOptionsDialog(
            product = ProductEntity(
                id = 1L,
                cartId = 0L,
                listId = 0L,
                name = "Produto Exemplo",
                quantity = 1.0,
                price = 10.0
            ),
            onDismiss = {},
            onEdit = {},
            onMoveToCart = {},
            onChangeQuantity = { _, _ -> },
            onDelete = {}
        )
    }
}
