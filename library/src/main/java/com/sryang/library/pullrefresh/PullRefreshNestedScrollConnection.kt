package com.sryang.library.pullrefresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * 중첩 스크롤 이벤트 감지
 */
internal class PullRefreshNestedScrollConnection(
    private val onPull: (pullDelta: Float) -> Float,
    private val onRelease: suspend (flingVelocity: Float) -> Float,
    private val enabled: Boolean
) : NestedScrollConnection {
    private val TAG = "__PullRefreshNestedScrollConnection"

    /**
     * "Pre-scroll 이벤트 체인"은 스크롤 이벤트가 자식 → 부모로 전달되는 과정에서,
     * 부모가 자식보다 먼저 이벤트를 소비할 기회를 갖는 메커니즘입니다.
     *
     * 자식 컴포넌트(예: LazyColumn, Scrollable)에서 스크롤 이벤트가 발생했을 때,
     * 부모 컴포넌트에게 이 이벤트를 처리할 기회를 제공하기 위해 호출됩니다.
     *
     * @param available available the delta available to consume for pre scroll
     *
     * delta
     * - 사용자가 스크롤을 수행했을 때 발생한 이동 거리(픽셀 단위).
     * - Offset 객체로 제공되며, 수평(x) 및 수직(y) 스크롤 거리를 포함합니다.
     * - 양수/음수 값으로 스크롤 방향을 나타냅니다:
     *      - y > 0: 아래로 스크롤.
     *      - y < 0: 위로 스크롤.
     *
     * available
     * - “부모가 소비할 수 있는 스크롤 거리”를 나타냅니다.
     * - 부모는 이 값을 사용해 일부 스크롤 이벤트를 처리하거나, 처리 후 남은 거리를 자식에게 전달합니다.
     */
    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero

        // swiping down을 하면 부모에게 소비할 값을 전달
        source == NestedScrollSource.UserInput && available.isSwipingUp -> {
            Log.d(TAG, "onPreScroll SwipingUp available:$available")
            Offset(0f, onPull(available.y))
        }
        // Swiping up을 하면 부모에게 소비할 값을 전달하지 않는다.
        else -> {
            Log.d(TAG, "onPreScroll SwipingDown available:$available")
            Offset.Zero
        }
    }

    /**
     * 	자식 컴포넌트가 스크롤 이벤트를 처리(소비)한 이후에,
     * 	남은 이벤트를 부모 컴포넌트로 전달하는 과정입니다.
     *
     * 	자식 컴포넌트가 스크롤 이벤트를 소비(consumption)한 뒤에,
     * 	Post-scroll 이벤트가 발생합니다.
     *
     * 	자식이 소비하지 않은 남은 스크롤 이벤트를 부모(ancestor)에게 알립니다.
     * 	부모는 이 남은 이벤트를 기반으로 추가적인 동작을 수행할 수 있습니다
     *
     * 	@param available 자식스크롤이 끝까지 도달해서 스크롤 중이지만 더이상 스크롤 이벤트 값을 처리 할 수 없을때 발생
     */
    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero

        // 자식 스크롤이 끝까지 도달 했는데 계속 스크롤 할 경우
        source == NestedScrollSource.UserInput && available.isSwipingDown -> {
            Log.d(TAG, "onPostScroll SwipingDown available:${available}")
            Log.d(TAG, "onPostScroll SwipingDown consumed:${consumed}")
            Offset(0f, onPull(available.y))
        }
        // Pulling down
        else -> {
            Log.d(TAG, "onPostScroll SwipingUp available:${available}")
            Log.d(TAG, "onPostScroll SwipingUp consumed:${consumed}")
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        return Velocity(0f, onRelease(available.y))
    }

    val Offset.isSwipingUp: Boolean get() = y < 0
    val Offset.isSwipingDown: Boolean get() = y > 0
}
