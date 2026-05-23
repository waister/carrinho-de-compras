package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.addPluralCharacter
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity

@Composable
fun CartHeader(
    total: Double,
    productCount: Int,
    volumes: Double,
) {
    Surface(
        color = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = total.formatPrice(),
                fontSize = 22.sp,
            )
            Text(
                text = stringResource(
                    R.string.products_details,
                    productCount,
                    productCount.addPluralCharacter(),
                    volumes.formatQuantity(),
                    volumes.addPluralCharacter(),
                ),
                fontSize = 13.sp,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CartHeaderPreview() {
    MyAppTheme {
        CartHeader(
            total = 150.50,
            productCount = 5,
            volumes = 12.0
        )
    }
}
