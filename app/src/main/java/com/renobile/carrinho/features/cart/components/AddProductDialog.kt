package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.features.cart.productPreview
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.parseCurrencyToDouble
import com.renobile.carrinho.util.parseToDouble
import java.text.NumberFormat

@Composable
fun AddProductDialog(
    product: ProductEntity?,
    onDismiss: () -> Unit = {},
    onConfirm: (String, Double, Double) -> Unit = { _, _, _ -> },
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantityText by remember { mutableStateOf(product?.quantity?.formatQuantity() ?: "1") }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    var priceTextFieldValue by remember {
        val initialText = product?.price?.let { if (it == 0.0) "" else it.formatPrice() } ?: ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(stringResource(if (product == null) R.string.add_product else R.string.edit_product))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*[.,]?\\d*$"))) {
                                quantityText = newValue
                            }
                        },
                        label = { Text(stringResource(R.string.quantity)) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next,
                        ),
                    )
                    IconButton(onClick = {
                        val current = quantityText.parseToDouble()
                        quantityText = (current + 1).formatQuantity()
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }
                    IconButton(onClick = {
                        val current = quantityText.parseToDouble()
                        if (current > 1) {
                            quantityText = (current - 1).formatQuantity()
                        }
                    }) {
                        Icon(painter = painterResource(R.drawable.ic_minus), contentDescription = null)
                    }
                }

                OutlinedTextField(
                    value = priceTextFieldValue,
                    onValueChange = { newValue ->
                        val digits = newValue.text.replace(Regex("\\D"), "")
                        val newText = if (digits.isEmpty() || digits.toLongOrNull() == 0L) {
                            ""
                        } else {
                            try {
                                val value = digits.toDouble() / 100
                                currencyFormatter.format(value)
                            } catch (_: Exception) {
                                ""
                            }
                        }
                        priceTextFieldValue = TextFieldValue(
                            text = newText,
                            selection = TextRange(newText.length)
                        )
                    },
                    label = { Text(stringResource(R.string.price)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (name.isNotBlank()) {
                                onConfirm(
                                    name,
                                    quantityText.parseToDouble(),
                                    priceTextFieldValue.text.parseCurrencyToDouble(),
                                )
                            }
                        },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            quantityText.parseToDouble(),
                            priceTextFieldValue.text.parseCurrencyToDouble(),
                        )
                    }
                },
            ) {
                Text(stringResource(if (product == null) R.string.add else R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(if (product == null) R.string.cancel else R.string.discard))
            }
        },
    )
}

@Preview
@Composable
private fun AddProductDialogNewPreview() {
    MyAppTheme {
        AddProductDialog(
            product = null,
        )
    }
}

@Preview
@Composable
private fun AddProductDialogEditPreview() {
    MyAppTheme {
        AddProductDialog(
            product = productPreview,
        )
    }
}
