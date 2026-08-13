package com.renobile.carrinho.features.cart.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.renobile.carrinho.R
import com.renobile.carrinho.database.entities.ProductEntity
import com.renobile.carrinho.database.entities.ProductSuggestion
import com.renobile.carrinho.features.cart.productPreview
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.util.formatDate
import com.renobile.carrinho.util.formatPrice
import com.renobile.carrinho.util.formatQuantity
import com.renobile.carrinho.util.parseCurrencyToDouble
import com.renobile.carrinho.util.parseToDouble
import java.text.NumberFormat

@Composable
fun AddProductDialog(
    product: ProductEntity?,
    suggestions: List<ProductSuggestion> = emptyList(),
    title: String? = null,
    message: String? = null,
    isPriceMandatory: Boolean = false,
    onDismiss: () -> Unit = {},
    onConfirm: (String, Double, Double) -> Unit = { _, _, _ -> },
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var quantityText by remember { mutableStateOf(product?.quantity?.formatQuantity() ?: "1") }
    var validationError by remember { mutableStateOf<Int?>(null) }

    var expanded by remember { mutableStateOf(false) }
    val filteredSuggestions = remember(name, suggestions) {
        if (name.length < 2) emptyList()
        else suggestions.filter { it.name.contains(name, ignoreCase = true) && it.name != name }.take(5)
    }

    val focusRequester = remember { FocusRequester() }
    val priceFocusRequester = remember { FocusRequester() }

    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    var priceTextFieldValue by remember {
        val initialText = product?.price?.let { if (it == 0.0) "" else it.formatPrice() } ?: ""
        mutableStateOf(TextFieldValue(text = initialText, selection = TextRange(initialText.length)))
    }

    val isPreview = LocalInspectionMode.current
    LaunchedEffect(Unit) {
        if (!isPreview) {
            if (product == null) {
                focusRequester.requestFocus()
            } else if (product.price == 0.0) {
                priceFocusRequester.requestFocus()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(title ?: stringResource(if (product == null) R.string.add_product else R.string.edit_product))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (message != null || validationError != null) {
                    Text(
                        text = validationError?.let { stringResource(it) } ?: message ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            expanded = true
                        },
                        label = { Text(stringResource(R.string.name)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                    )

                    if (filteredSuggestions.isNotEmpty()) {
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            properties = PopupProperties(focusable = false),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            filteredSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(suggestion.name, modifier = Modifier.weight(1f))
                                            Text(
                                                text = suggestion.lastDate.formatDate(),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        name = suggestion.name
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

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
                        validationError = null
                    },
                    label = { Text(stringResource(R.string.price)) },
                    isError = validationError == R.string.error_price_mandatory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(priceFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val priceValue = priceTextFieldValue.text.parseCurrencyToDouble()
                            if (name.isBlank()) {
                                validationError = R.string.error_name_mandatory
                                focusRequester.requestFocus()
                            } else if (isPriceMandatory && priceValue <= 0.0) {
                                validationError = R.string.error_price_mandatory
                                priceFocusRequester.requestFocus()
                            } else {
                                onConfirm(
                                    name,
                                    quantityText.parseToDouble(),
                                    priceValue,
                                )
                                if (product == null) {
                                    name = ""
                                    quantityText = "1"
                                    priceTextFieldValue = TextFieldValue("")
                                    validationError = null
                                    focusRequester.requestFocus()
                                }
                            }
                        },
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val priceValue = priceTextFieldValue.text.parseCurrencyToDouble()
                    if (name.isBlank()) {
                        validationError = R.string.error_name_mandatory
                        focusRequester.requestFocus()
                    } else if (isPriceMandatory && priceValue <= 0.0) {
                        validationError = R.string.error_price_mandatory
                        priceFocusRequester.requestFocus()
                    } else {
                        onConfirm(
                            name,
                            quantityText.parseToDouble(),
                            priceValue,
                        )
                        if (product == null) {
                            name = ""
                            quantityText = "1"
                            priceTextFieldValue = TextFieldValue("")
                            validationError = null
                            focusRequester.requestFocus()
                        }
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
