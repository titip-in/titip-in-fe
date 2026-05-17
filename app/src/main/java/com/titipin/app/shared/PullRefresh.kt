package com.titipin.app.shared

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.titipin.app.ui.theme.Cream
import com.titipin.app.ui.theme.Terracotta

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
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                color = Terracotta,
                containerColor = Cream
            )
        },
        content      = { content() }
    )
}
