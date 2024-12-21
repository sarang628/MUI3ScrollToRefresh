package com.sryang.mui3scrolltorefresh

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sryang.library.pullrefresh.PullToRefreshLayout
import com.sryang.mui3scrolltorefresh.ui.theme.MUI3ScrollToRefreshTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val coroutine = rememberCoroutineScope()
            MUI3ScrollToRefreshTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state = com.sryang.library.pullrefresh.rememberPullToRefreshState()

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
                        LazyColumn(
                            content = {
                                items(100) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp),
                                        text = "$it"
                                    )
                                }
                            },
                            userScrollEnabled = true
                        )
                    }
                }
            }
        }
    }
}