package com.sryang.library.pullrefresh

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow

class PullToRefreshLayoutState {

    private val _lastRefreshTime: MutableStateFlow<Long> =
        MutableStateFlow(System.currentTimeMillis())

    var refreshIndicatorState = mutableStateOf(RefreshIndicatorState.Default)
        private set

    fun updateState(refreshState: RefreshIndicatorState) {
        refreshIndicatorState.value = refreshState
    }

    fun refresh() {
        _lastRefreshTime.value = System.currentTimeMillis()
        updateState(RefreshIndicatorState.Refreshing)
    }
}

@Composable
fun rememberPullToRefreshState(): PullToRefreshLayoutState =
    remember {
        PullToRefreshLayoutState()
    }
