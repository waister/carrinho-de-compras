package com.renobile.carrinho.features.list.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.renobile.carrinho.R

@Composable
fun EmptyListView(
    onCreateList: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.lists_empty),
                modifier = Modifier.padding(16.dp)
            )
            Button(onClick = onCreateList) {
                Text(stringResource(R.string.create_list))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyListViewPreview() {
    MaterialTheme {
        EmptyListView()
    }
}
