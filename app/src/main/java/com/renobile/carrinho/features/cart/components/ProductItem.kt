package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.productPreview
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity

@Composable
fun ProductItem(
    product: ProductEntity,
    onClick: () -> Unit = {},
    onMoveToCart: (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = { Text(product.name) },
        supportingContent = {
            Text(
                stringResource(
                    R.string.product_details,
                    product.quantity.formatQuantity(),
                    product.quantity.addPluralCharacter(),
                    product.price.formatPrice(),
                ),
            )
        },
        leadingContent = if (onMoveToCart != null) {
            {
                Icon(
                    painter = painterResource(R.drawable.ic_cart_plus),
                    contentDescription = stringResource(R.string.move_to_cart),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { onMoveToCart() }
                        .padding(4.dp),
                )
            }
        } else {
            null
        },
        trailingContent = {
            Text((product.quantity * product.price).formatPrice())
        },
        modifier = Modifier.clickable { onClick() },
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductItemPreview() {
    MyAppTheme {
        ProductItem(
            product = productPreview,
        )
    }
}
