package com.sryang.library.pullrefresh

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PullToRefreshIndicator(
    indicatorState: RefreshIndicatorState,
    pullToRefreshProgress: Float,
    height: Int = 100,
    indicatorSize: Dp = 40.dp,
    indicatorColor: Color = MaterialTheme.colorScheme.primary,
    pullDownIndicatorColor: Color = MaterialTheme.colorScheme.inversePrimary,
) {
    val heightModifier = when (indicatorState) {
        RefreshIndicatorState.PullingDown -> {
            Modifier.height(
                (pullToRefreshProgress * 100)
                    .roundToInt()
                    .coerceAtMost(height).dp,
            )
        }

        RefreshIndicatorState.ReachedThreshold -> Modifier.height(height.dp)
        RefreshIndicatorState.Refreshing -> Modifier.wrapContentHeight()
        RefreshIndicatorState.Default -> Modifier.height(0.dp)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .then(heightModifier)
            .padding(15.dp),
        contentAlignment = Alignment.BottomStart,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (indicatorState == RefreshIndicatorState.Refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(indicatorSize),
                    color = indicatorColor,
                    strokeWidth = 2.dp,
                )
            } else if (indicatorState == RefreshIndicatorState.PullingDown) {
                CircularProgressIndicator(
                    modifier = Modifier.size(indicatorSize),
                    color = pullDownIndicatorColor,
                    strokeWidth = 2.dp,
                    progress = pullToRefreshProgress
                )
            } else if (indicatorState == RefreshIndicatorState.ReachedThreshold) {
                CircularProgressIndicator(
                    modifier = Modifier.size(indicatorSize),
                    color = indicatorColor,
                    strokeWidth = 2.dp,
                    progress = pullToRefreshProgress
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewPullToRefreshIndicator() {
    PullToRefreshIndicator(
        indicatorState = RefreshIndicatorState.PullingDown,
        pullToRefreshProgress = 0.5f,
        height = 100
    )
}