package com.renobile.carrinho.features.comparator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R
import com.renobile.carrinho.util.fromHtml
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparatorScreen(
    viewModel: ComparatorViewModel,
    onShare: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.confirmation)) },
            text = { Text(stringResource(R.string.confirmation_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clear()
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
            TopAppBar(
                title = { Text(stringResource(R.string.comparator)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
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
                onPriceChange = viewModel::onPriceFirstChanged,
                size = state.sizeFirst,
                onSizeChange = viewModel::onSizeFirstChanged
            )

            HorizontalDivider()

            ProductInputSection(
                title = stringResource(R.string.label_second),
                price = state.priceSecond,
                onPriceChange = viewModel::onPriceSecondChanged,
                size = state.sizeSecond,
                onSizeChange = viewModel::onSizeSecondChanged,
                imeAction = ImeAction.Done,
                onDone = { viewModel.calculate() }
            )

            Button(
                onClick = { viewModel.calculate() },
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
fun ProductInputSection(
    title: String,
    price: String,
    onPriceChange: (String) -> Unit,
    size: String,
    onSizeChange: (String) -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
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
fun MoneyField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val currencyFormatter = remember { NumberFormat.getCurrencyInstance() }

    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val digits = newValue.replace(Regex("\\D"), "")
            if (digits.isEmpty()) {
                onValueChange("")
            } else {
                try {
                    val doubleValue = digits.toDouble() / 100
                    onValueChange(currencyFormatter.format(doubleValue))
                } catch (_: Exception) {}
            }
        },
        label = { Text(label) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        singleLine = true
    )
}

@Composable
fun ResultSection(state: ComparatorState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            state.resultFirst?.let {
                Text(text = it.fromHtml().toString(), fontSize = 18.sp)
            }
            state.resultSecond?.let {
                Text(text = it.fromHtml().toString(), fontSize = 18.sp)
            }
            state.resultPercentage?.let {
                Text(
                    text = it.fromHtml().toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
