package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun EmptyCartView(
    isCartCreated: Boolean,
    modifier: Modifier = Modifier,
    onCreateCart: () -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(if (!isCartCreated) R.string.carts_empty else R.string.products_empty),
                modifier = Modifier.padding(16.dp)
            )
            if (!isCartCreated) {
                Button(onClick = onCreateCart) {
                    Text(stringResource(R.string.create_cart))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyCartViewNoCartPreview() {
    MyAppTheme {
        EmptyCartView(isCartCreated = false)
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyCartViewWithCartPreview() {
    MyAppTheme {
        EmptyCartView(isCartCreated = true)
    }
}
