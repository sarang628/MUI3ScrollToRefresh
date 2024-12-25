package com.sryang.library.pullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * pull to refresh 레이아웃
 * @param pullRefreshLayoutState pull to refresh 상태. 스스로 상태를 변경 시킬 수 있음
 */
@Composable
fun PullToRefreshLayout(
    modifier: Modifier = Modifier,
    pullRefreshLayoutState: PullToRefreshLayoutState,
    onRefresh: () -> Unit,
    refreshThreshold: Int = 120,
    content: @Composable () -> Unit,
) {
    val refreshIndicatorState by pullRefreshLayoutState.refreshIndicatorState

    val pullToRefreshState = rememberPullRefreshState(
        refreshing = refreshIndicatorState == RefreshIndicatorState.Refreshing,
        refreshThreshold = refreshThreshold.dp,
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
        modifier = modifier.nestedScrollForPullRefresh(pullToRefreshState),
    ) {
        PullToRefreshIndicator(
            indicatorState = refreshIndicatorState,
            pullToRefreshProgress = pullToRefreshState.progress,
            height = refreshThreshold
        )
        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewPullToRefreshLayout() {
    val state = rememberPullToRefreshState()
    val coroutine = rememberCoroutineScope()
    PullToRefreshLayout(
        pullRefreshLayoutState = state,
        onRefresh = {
            coroutine.launch {
                delay(1000)
                state.updateState(RefreshIndicatorState.Default)
            }
        },
        content = {
            LazyColumn {
                items(101) {
                    Text(text = "Item $it")
                }
            }
        }
    )
}
