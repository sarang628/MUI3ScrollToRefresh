package com.sryang.mui3scrolltorefresh.test

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SwipeToRefresh(
    modifier: Modifier = Modifier,
    onLoad: () -> Unit,
    triggerHeight: Float,
    contents: @Composable (Modifier) -> Unit,
    isLoaded: Boolean
) {
    var offsetY by remember { mutableStateOf(0f) }
    var isDrag by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var isLoading by remember { mutableStateOf(false) }

    val showProgress = !isLoading

    LaunchedEffect(key1 = isDrag, block = {
        // 드레그가 끝났다면
        if (!isDrag) {
            // 원 위치로 되돌리기
            while (offsetY > 0) {
                delay(5)
                //로딩중이라면 프로그레스 아래까지 되돌리기
                if (isLoading && offsetY > triggerHeight) {
                    offsetY -= 10
                    if (offsetY < triggerHeight) {
                        offsetY = triggerHeight
                        break
                    }
                }
                // 로딩중이 아니라면 원 위치로 되돌리기
                else if (!isLoading && offsetY > 0) {
                    offsetY -= 10
                    if (offsetY < 0) {
                        offsetY = 0f
                        progress = 0f
                        break
                    }
                    progress = offsetY / triggerHeight
                }
            }
        }
    })

    // 로딩이 끝났을 때 원 위치로 되돌리기
    LaunchedEffect(key1 = isLoaded, block = {
        if (!isLoaded) {
            while (offsetY > 0) {
                delay(10)
                if (offsetY > 0) {
                    offsetY -= 20

                    if (offsetY <= 0) {
                        offsetY = 0f
                        isLoading = false
                    }

                    if (isLoaded)
                        progress = offsetY / triggerHeight
                }
            }
        }
    })

    Box(Modifier.background(Color.LightGray)) {
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 5.dp)
        ) {
            if (showProgress) { //
                CircularProgressIndicator(progress = progress)
            } else {
                CircularProgressIndicator()
            }
        }


        Box(
            modifier = modifier
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .fillMaxWidth()
                .height(300.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .background(color = MaterialTheme.colorScheme.background)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = {
                            isDrag = true
                        },
                        onDragEnd = {
                            isDrag = false
                        }
                    ) { change, dragAmount ->
                        change.consume()

                        if (isLoading && dragAmount.y < 0)
                            return@detectDragGestures

                        if (offsetY + dragAmount.y < 0)
                            return@detectDragGestures

                        progress = offsetY / triggerHeight
                        if (progress >= 1.0f) {
                            isLoading = true
                            onLoad.invoke()
                        }
                        offsetY += (dragAmount.y - dragAmount.y / 2f)
                    }
                }
        ) {
            contents.invoke(modifier)
        }
    }
}