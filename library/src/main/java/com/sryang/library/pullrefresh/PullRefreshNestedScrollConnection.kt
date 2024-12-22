package com.sryang.library.pullrefresh

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

internal class PullRefreshNestedScrollConnection(
    private val onPull: (pullDelta: Float) -> Float,
    private val onRelease: suspend (flingVelocity: Float) -> Float,
    private val enabled: Boolean
) : NestedScrollConnection {
    private val TAG = "__PullRefreshNestedScrollConnection"

    /**
     * 자식이 스크롤 될 경우 이곳에 offset 값이 들어옴
     */
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.UserInput && available.y < 0 -> {
            Log.d(TAG, "onPreScroll available.y(${available.y})")
            Offset(0f, onPull(available.y))
        }
        // Swiping up
        else -> {
            Log.d(TAG, "onPreScroll available.y(${available.y})")
            Offset.Zero
        }
    }

    /**
     * 부모가 스크롤 될 경우 이곳에 offset 값이 들어옴
     */
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.UserInput && available.y > 0 -> {
            Log.d(TAG, "onPostScroll available.y(${available.y})")
            Offset(0f, onPull(available.y))
        }
        // Pulling down
        else -> {
            Log.d(TAG, "onPostScroll available.y(${available.y})")
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return Velocity(0f, onRelease(available.y))
    }
}
