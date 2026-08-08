package com.renobile.carrinho.features.comparator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.ui.theme.MyAppTheme
import com.renobile.carrinho.ui.theme.TextPrimary
import com.renobile.carrinho.ui.theme.TextSecondary
import com.renobile.carrinho.util.fromHtml
import java.text.NumberFormat

@Composable
fun ComparatorScreen(
    viewModel: ComparatorViewModel,
    onShare: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    ComparatorScreen(
        state = state,
        onPriceFirstChanged = viewModel::onPriceFirstChanged,
        onSizeFirstChanged = viewModel::onSizeFirstChanged,
        onPriceSecondChanged = viewModel::onPriceSecondChanged,
        onSizeSecondChanged = viewModel::onSizeSecondChanged,
        onClear = viewModel::clear,
        onCalculate = { viewModel.calculate() },
        onShare = onShare
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorScreen(
    state: ComparatorState,
    onPriceFirstChanged: (String) -> Unit = {},
    onSizeFirstChanged: (String) -> Unit = {},
    onPriceSecondChanged: (String) -> Unit = {},
    onSizeSecondChanged: (String) -> Unit = {},
    onClear: () -> Unit = {},
    onCalculate: () -> Unit = {},
    onShare: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.showResult) {
        if (state.showResult) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(stringResource(R.string.confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onClear()
                    showClearDialog = false
                }) { Text(stringResource(R.string.clear)) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                TopAppBar(
                    title = { Text(stringResource(R.string.comparator)) },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                        IconButton(onClick = onShare) {
                            Icon(Icons.Default.Share, contentDescription = null)
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProductInputSection(
                title = stringResource(R.string.label_first),
                price = state.priceFirst,
                onPriceChange = onPriceFirstChanged,
                size = state.sizeFirst,
                onSizeChange = onSizeFirstChanged
            )

            HorizontalDivider()

            ProductInputSection(
                title = stringResource(R.string.label_second),
                price = state.priceSecond,
                onPriceChange = onPriceSecondChanged,
                size = state.sizeSecond,
                onSizeChange = onSizeSecondChanged,
                imeAction = ImeAction.Done,
                onDone = onCalculate
            )

            Button(
                onClick = onCalculate,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.button_submit))
            }

            if (state.showResult) {
                ResultSection(state)
            }
        }
    }
}

@Composable
private fun ProductInputSection(
    title: String,
    price: String,
    onPriceChange: (String) -> Unit,
    size: String,
    onSizeChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {},
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MoneyField(
                value = price,
                onValueChange = onPriceChange,
                label = stringResource(R.string.text_price),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = size,
                onValueChange = onSizeChange,
                label = { Text(stringResource(R.string.text_size)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                singleLine = true
            )
        }
    }
}

@Composable
private fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }
    val textFieldValue = remember(value) {
        TextFieldValue(text = value, selection = TextRange(value.length))
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { newValue: TextFieldValue ->
            val digits = newValue.text.replace(Regex("\\D"), "")
            val newText = if (digits.isEmpty() || digits.toLongOrNull() == 0L) {
                ""
            } else {
                try {
                    val doubleValue = digits.toDouble() / 100
                    currencyFormatter.format(doubleValue)
                } catch (_: Exception) {
                    ""
                }
            }
            if (newText != value) {
                onValueChange(newText)
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true
    )
}

@Composable
private fun ResultSection(state: ComparatorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.resultFirst?.let {
                Text(
                    text = it.fromHtml().toString(),
                    color = TextSecondary,
                )
            }
            state.resultSecond?.let {
                Text(
                    text = it.fromHtml().toString(),
                    color = TextSecondary,
                )
            }
            state.resultPercentage?.let {
                Text(
                    text = it.fromHtml().toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ComparatorScreenPreview() {
    MyAppTheme {
        ComparatorScreen(
            state = ComparatorState(
                priceFirst = "R$ 10,00",
                sizeFirst = "1000",
                priceSecond = "R$ 8,00",
                sizeSecond = "500",
                resultFirst = "O primeiro custa R$ 0,01 por unidade",
                resultSecond = "O segundo custa R$ 0,16 por unidade",
                resultPercentage = "O primeiro é 37,5% mais barato",
                showResult = true,
            )
        )
    }
}
