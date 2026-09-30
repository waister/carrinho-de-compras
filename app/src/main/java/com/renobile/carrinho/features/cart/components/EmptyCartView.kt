package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme

@Composable
fun EmptyCartView(
    isCartCreated: Boolean,
    modifier: Modifier = Modifier,
    onCreateCart: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(if (!isCartCreated) R.string.carts_empty else R.string.products_empty),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (!isCartCreated) {
                Button(
                    onClick = onCreateCart,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(stringResource(R.string.create_cart))
                }
            } else {
                Button(
                    onClick = onNavigateToList,
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_format_list_checks),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.empty_cart_go_to_list))
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
