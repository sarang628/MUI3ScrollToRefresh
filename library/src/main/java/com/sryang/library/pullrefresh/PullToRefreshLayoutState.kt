package com.sryang.library.pullrefresh

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * pull to refresh 레이아웃 상태
 *
 * 리프레시 인디케이터만 상태를 관리함.
 */
class PullToRefreshLayoutState {

    /** 리프레시 인디케이터 상태 */
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
