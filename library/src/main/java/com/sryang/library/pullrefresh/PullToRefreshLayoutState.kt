package com.sryang.library.pullrefresh

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow

class PullToRefreshLayoutState {

    var refreshIndicatorState = mutableStateOf(RefreshIndicatorState.Default)
        private set

    fun updateState(refreshState: RefreshIndicatorState) {
        refreshIndicatorState.value = refreshState
    }

    fun refresh() {
        updateState(RefreshIndicatorState.Refreshing)
    }
}

@Composable
fun rememberPullToRefreshState(): PullToRefreshLayoutState =
    remember {
        PullToRefreshLayoutState()
    }
