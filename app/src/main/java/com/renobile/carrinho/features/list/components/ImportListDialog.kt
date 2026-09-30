package com.renobile.carrinho.features.list.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R
import com.renobile.carrinho.util.TextRecognitionHelper
import kotlinx.coroutines.launch

@Composable
fun ImportListDialog(
    initialText: String = "",
    onDismiss: () -> Unit = {},
    onConfirm: (List<String>) -> Unit = {},
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var isLoadingImage by remember { mutableStateOf(false) }
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            isLoadingImage = true
            scope.launch {
                TextRecognitionHelper.extractShoppingListFromImage(context, uri)
                    .onSuccess { items ->
                        if (items.isNotEmpty()) {
                            val newText = items.joinToString("\n")
                            text = if (text.isBlank()) newText else "${text.trim()}\n$newText"
                        } else {
                            Toast.makeText(context, R.string.no_items_found_in_image, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .onFailure {
                        Toast.makeText(context, R.string.image_read_error, Toast.LENGTH_SHORT).show()
                    }
                isLoadingImage = false
            }
        }
    }

    val processedItems = remember(text) {
        text.split(Regex("\\r?\\n"))
            .map { it.trim() }
            .map { it.replace(Regex("^\\[\\s*]"), "").trim() }
            .filter { it.isNotEmpty() }
    }

    AlertDialog(
        onDismissRequest = {
            if (!isLoadingImage) onDismiss()
        },
        title = { Text(stringResource(R.string.import_list)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 450.dp),
            ) {
                Text(
                    text = stringResource(R.string.import_list_description),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
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
                    ),
                    enabled = !isLoadingImage,
                )

                if (isLoadingImage) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.processing_image),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                if (processedItems.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.import_list_preview, processedItems.size),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                    Text(
                        text = processedItems.take(5).joinToString(", ") + if (processedItems.size > 5) "..." else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        onClick = {
                            pickImageLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                        enabled = !isLoadingImage,
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.import_from_image))
                    }

                    Spacer(Modifier.width(4.dp))

                    TextButton(
                        onClick = {
                            scope.launch {
                                clipboard.getClipEntry()?.let { entry ->
                                    val pasteText = entry.clipData.getItemAt(0).text?.toString() ?: ""
                                    if (pasteText.isNotBlank()) {
                                        text = if (text.isBlank()) pasteText else "${text.trim()}\n$pasteText"
                                    }
                                }
                            }
                        },
                        enabled = !isLoadingImage,
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.paste))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(processedItems) },
                enabled = processedItems.isNotEmpty() && !isLoadingImage,
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoadingImage,
            ) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
private fun ImportListDialogPreview() {
    MaterialTheme {
        ImportListDialog()
    }
}
