package com.renobile.carrinho.features.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import kotlinx.coroutines.launch

@Composable
fun ImportListDialog(
    onDismiss: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val processedItems = remember(text) {
        text.split(Regex("\\r?\\n"))
            .map { it.trim() }
            .map { it.replace(Regex("^\\[\\s*]"), "").trim() }
            .filter { it.isNotEmpty() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.import_list)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Text(
                    text = stringResource(R.string.import_list_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text(stringResource(R.string.import_list_placeholder)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                    )
                )

                if (processedItems.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.import_list_preview, processedItems.size),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                    Text(
                        text = processedItems.take(5).joinToString(", ") + if (processedItems.size > 5) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        scope.launch {
                            clipboard.getClipEntry()?.let { entry ->
                                text = entry.clipData.getItemAt(0).text?.toString() ?: ""
                            }
                        }
                    }) {
                        Icon(Icons.Default.ContentPaste, null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.paste))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(processedItems) },
                enabled = processedItems.isNotEmpty()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
fun ImportListDialogPreview() {
    MaterialTheme {
        ImportListDialog()
    }
}
