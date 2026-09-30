package com.renobile.carrinho.features.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.renobile.carrinho.R
import com.renobile.carrinho.network.models.NotificationModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    viewModel: NotificationsViewModel,
    onBackClick: () -> Unit,
    onNotificationClick: (String) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                TopAppBar(
                    title = { Text(stringResource(R.string.notifications)) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    },
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                    ),
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            } else if (state.notifications.isEmpty()) {
                Text(
                    text = stringResource(R.string.notifications_empty),
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.notifications) { notification ->
                        NotificationItem(notification = notification, onClick = {
                            onNotificationClick(notification.id)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: NotificationModel, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(notification.title) },
        supportingContent = {
            Column {
                Text(notification.body, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                Text(notification.date, fontSize = 12.sp, color = Color.Gray)
            }
        },
        trailingContent = {
            if (notification.image != null) {
                AsyncImage(
                    model = notification.image,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Crop,
                )
            }
        },
        modifier = Modifier.clickable { onClick() },
    )
}
