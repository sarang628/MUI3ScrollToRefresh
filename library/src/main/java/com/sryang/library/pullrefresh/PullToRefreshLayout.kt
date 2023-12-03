package com.sryang.library.pullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PullToRefreshLayout(
    modifier: Modifier = Modifier,
    pullRefreshLayoutState: PullToRefreshLayoutState,
    onRefresh: () -> Unit,
    content: @Composable () -> Unit,
) {
    val refreshIndicatorState by pullRefreshLayoutState.refreshIndicatorState

    val pullToRefreshState = rememberPullRefreshState(
        refreshing = refreshIndicatorState == RefreshIndicatorState.Refreshing,
        refreshThreshold = 120.dp,
        onRefresh = {
            onRefresh()
            pullRefreshLayoutState.refresh()
        },
    )

    LaunchedEffect(key1 = pullToRefreshState.progress) {
        when {
            pullToRefreshState.progress >= 1 -> {
                pullRefreshLayoutState.updateState(RefreshIndicatorState.ReachedThreshold)
            }

            pullToRefreshState.progress > 0 -> {
                pullRefreshLayoutState.updateState(RefreshIndicatorState.PullingDown)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullToRefreshState),
    ) {
        PullToRefreshIndicator(
            indicatorState = refreshIndicatorState,
            pullToRefreshProgress = pullToRefreshState.progress,
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }

}
