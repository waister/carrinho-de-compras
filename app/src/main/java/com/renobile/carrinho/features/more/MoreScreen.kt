package com.renobile.carrinho.features.more

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.renobile.carrinho.R
import com.renobile.carrinho.util.shareApp
import com.renobile.carrinho.util.storeAppLink

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNotifications: () -> Unit,
    onAbout: () -> Unit,
    onShare: () -> Unit,
    onRemoveAds: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.more)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            MoreMenuItem(
                title = stringResource(R.string.notifications),
                icon = R.drawable.ic_bell,
                onClick = onNotifications
            )
            MoreMenuItem(
                title = stringResource(R.string.about_app),
                icon = R.drawable.ic_information_outline,
                onClick = onAbout
            )
            MoreMenuItem(
                title = stringResource(R.string.share_app),
                icon = R.drawable.ic_share_variant,
                onClick = onShare
            )
            MoreMenuItem(
                title = stringResource(R.string.rate_app),
                icon = R.drawable.ic_star,
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, storeAppLink().toUri())
                    context.startActivity(intent)
                }
            )
            MoreMenuItem(
                title = stringResource(R.string.remove_ads),
                icon = R.drawable.ic_crown,
                onClick = onRemoveAds
            )
        }
    }
}

@Composable
fun MoreMenuItem(title: String, icon: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable { onClick() }
    )
}
