package com.sryang.mui3scrolltorefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollDispatcher
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sryang.library.pullrefresh.PullToRefreshLayout
import com.sryang.mui3scrolltorefresh.ui.theme.MUI3ScrollToRefreshTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MUI3ScrollToRefreshTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PullToRefreshTest()
                }
            }
        }
    }
}

@Composable
fun PullToRefreshTest() {
    val state = com.sryang.library.pullrefresh.rememberPullToRefreshState()
    val coroutine = rememberCoroutineScope()

    PullToRefreshLayout(
        modifier = Modifier.fillMaxSize(),
        pullRefreshLayoutState = state,
        refreshThreshold = 70,
        onRefresh = {
            coroutine.launch {
                delay(1000)
                state.updateState(com.sryang.library.pullrefresh.RefreshIndicatorState.Default)
            }
        }
    ) {
        TestLazyColumn()
    }
}

@Composable
fun TestLazyColumn() {
    LazyColumn(
        content = {
            items(100) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp), text = "$it"
                )
            }
        },
        userScrollEnabled = true
    )
}
