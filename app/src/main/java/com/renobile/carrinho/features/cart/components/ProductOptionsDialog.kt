package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.productPreview
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun ProductOptionsDialog(
    product: ProductEntity,
    onDismiss: () -> Unit = {},
    onEdit: (ProductEntity) -> Unit = {},
    onChangeQuantity: (ProductEntity, Double) -> Unit = { _, _ -> },
    onDelete: (ProductEntity) -> Unit = {},
) {
    val options = stringArrayResource(R.array.product_cart_options)
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(min = 280.dp, max = 560.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                ListItem(
                    headlineContent = { Text(options.getOrNull(0) ?: stringResource(R.string.edit_product)) },
                    modifier = Modifier.clickable { onEdit(product) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(1) ?: "+ 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, 1.0) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(2) ?: "- 1.0") },
                    modifier = Modifier.clickable { onChangeQuantity(product, -1.0) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
                ListItem(
                    headlineContent = { Text(options.getOrNull(3) ?: "Excluir") },
                    modifier = Modifier.clickable { onDelete(product) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ProductOptionsDialogPreview() {
    MyAppTheme {
        ProductOptionsDialog(
            product = productPreview,
        )
    }
}
