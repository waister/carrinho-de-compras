package com.renobile.carrinho.features.removeads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.renobile.carrinho.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoveAdsScreen(
    state: RemoveAdsState,
    actions: RemoveAdsActions,
) {
    if (state.showRestartDialog) {
        AlertDialog(
            onDismissRequest = actions.onDismissRestart,
            title = { Text(stringResource(R.string.plan_success_title)) },
            text = { Text(stringResource(R.string.plan_success_body)) },
            confirmButton = {
                TextButton(onClick = actions.onRestart) {
                    Text(stringResource(R.string.restart_app))
                }
            },
            dismissButton = {
                TextButton(onClick = actions.onDismissRestart) {
                    Text(stringResource(R.string.later))
                }
            },
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
            ) {
                TopAppBar(
                    title = { Text(stringResource(R.string.remove_ads)) },
                    navigationIcon = {
                        IconButton(onClick = actions.onBack) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.haveVideoPlan) {
                Text(
                    text = stringResource(R.string.thanks),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = stringResource(R.string.watch_to_by_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = state.description,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = actions.onWatchClick,
                        enabled = state.isAdReady,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.watch_to_by_button))
                    }
                }
            }
        }
    }
}
