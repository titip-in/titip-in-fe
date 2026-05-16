package com.titipin.app.shared

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Wrapper composable pull-to-refresh yang dipakai di semua list screen.
 * Pakai Material3 PullToRefreshBox (BOM 2024.02+).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitipinPullRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh    = onRefresh,
        state        = state,
        modifier     = modifier,
        content      = { content() }
    )
}