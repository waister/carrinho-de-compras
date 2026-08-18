package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.util.ProductSortOrder

@Composable
fun SortOptionsDialog(
    currentOrder: ProductSortOrder,
    onDismiss: () -> Unit,
    onSortOrderSelected: (ProductSortOrder) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sort_order)) },
        text = {
            Column {
                SortOptionItem(
                    label = stringResource(R.string.sort_newest),
                    selected = currentOrder == ProductSortOrder.NEWEST,
                    onClick = { onSortOrderSelected(ProductSortOrder.NEWEST) },
                )
                SortOptionItem(
                    label = stringResource(R.string.sort_oldest),
                    selected = currentOrder == ProductSortOrder.OLDEST,
                    onClick = { onSortOrderSelected(ProductSortOrder.OLDEST) },
                )
                SortOptionItem(
                    label = stringResource(R.string.sort_name_asc),
                    selected = currentOrder == ProductSortOrder.NAME_ASC,
                    onClick = { onSortOrderSelected(ProductSortOrder.NAME_ASC) },
                )
                SortOptionItem(
                    label = stringResource(R.string.sort_name_desc),
                    selected = currentOrder == ProductSortOrder.NAME_DESC,
                    onClick = { onSortOrderSelected(ProductSortOrder.NAME_DESC) },
                )
                SortOptionItem(
                    label = stringResource(R.string.sort_price_asc),
                    selected = currentOrder == ProductSortOrder.PRICE_ASC,
                    onClick = { onSortOrderSelected(ProductSortOrder.PRICE_ASC) },
                )
                SortOptionItem(
                    label = stringResource(R.string.sort_price_desc),
                    selected = currentOrder == ProductSortOrder.PRICE_DESC,
                    onClick = { onSortOrderSelected(ProductSortOrder.PRICE_DESC) },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        },
    )
}

@Composable
private fun SortOptionItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
