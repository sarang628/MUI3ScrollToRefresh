package com.sryang.library.pullrefresh

import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

/**
 * A state object that can be used in conjunction with [nestedScrollForPullRefresh] to add pull-to-refresh
 * behaviour to a scroll component. Based on Android's SwipeRefreshLayout.
 *
 * Provides [progress], a float representing how far the user has pulled as a percentage of the
 * refreshThreshold. Values of one or less indicate that the user has not yet pulled past the
 * threshold. Values greater than one indicate how far past the threshold the user has pulled.
 *
 * Can be used in conjunction with [pullRefreshIndicatorTransform] to implement Android-like
 * pull-to-refresh behaviour with a custom indicator.
 *
 * Should be created using [rememberPullRefreshState].
 */
class PullRefreshState internal constructor(
    private val animationScope: CoroutineScope,
    private val onRefreshState: State<() -> Unit>,
    refreshingOffset: Float,
    threshold: Float
) {
    private val tag = "__PullRefreshState"

    /**
     * The distance pulled is multiplied by this value to give us the adjusted distance pulled, which
     * is used in calculating the indicator position (when the adjusted distance pulled is less than
     * the refresh threshold, it is the indicator position, otherwise the indicator position is
     * derived from the progress).
     */
    private val DragMultiplier = 0.5f

    /** 부모가 당겨진 값 */
    private var distancePulled by mutableFloatStateOf(0f)

    /** 임계값 */
    private var threshold by mutableFloatStateOf(threshold)
    private var refreshingOffset by mutableFloatStateOf(refreshingOffset)

    /**
     * 부모가 당겨진 값 * 드래그 감도
     */
    private val adjustedDistancePulled by derivedStateOf { distancePulled * DragMultiplier }

    /**
     * A float representing how far the user has pulled as a percentage of the refreshThreshold.
     *
     * If the component has not been pulled at all, progress is zero. If the pull has reached
     * halfway to the threshold, progress is 0.5f. A value greater than 1 indicates that pull has
     * gone beyond the refreshThreshold - e.g. a value of 2f indicates that the user has pulled to
     * two times the refreshThreshold.
     */
    val progress get() = adjustedDistancePulled / threshold

    /** 갱신중 여부 */
    private var refreshing by mutableStateOf(false)
    private var position by mutableFloatStateOf(0f)


    /**
     * 당김 이벤트 처리
     *
     * 함수가 호출되는 case
     *
     * 1. 자식뷰를 swiping up 할 경우
     *  - 부모가 당겨진 상태(프로그레스바가 보여지는 상태)에서 스와이프를 올릴 경우 이를 처리해야해서 필요
     *
     * 2. 자식뷰가 swiping down 할 경우(자식뷰가 끝까지 올라간 상태에서)
     *   - 부모를 아래로 당기기(프로그레스바를 보여지게) 위해
     *
     * @param pullDelta 입력된 드레그 거리 swipedown을 하면 양수 swipeup을 하면 음수가 들어옴
     *
     * @return 프로그레스바가 표시되어야 하는 높이 (부모를 새로 당겨야 total 값  - 이전 부모가 당겨진 total 값)
     * */
    internal fun onPull(pullDelta: Float): Float {
        if (refreshing) return 0f // Already refreshing, do nothing.

        /**
         * 부모가 당겨진 값 갱신
         *
         * 부모가 당겨진 값  + 스와이프 업(-)/다운(+) 값
         * 결과가 음수이면 0으로 처리한다.
         *
         *  */
        val newDistancePulled = (distancePulled + pullDelta).coerceAtLeast(0f)

        val dragConsumed = newDistancePulled - distancePulled
        distancePulled = newDistancePulled
        position = calculateIndicatorPosition()

        Log.d(
            tag,
            "부모가 당겨진 값($distancePulled) + 스와이프 업/다운:($pullDelta) = 새로 당겨진 값: ${distancePulled + pullDelta}, dragConsumed :${dragConsumed}"
        )
        return dragConsumed
    }

    internal fun onRelease(velocity: Float): Float {
        if (refreshing) return 0f // Already refreshing, do nothing

        if (adjustedDistancePulled > threshold) {
            onRefreshState.value()
        }
        animateIndicatorTo(0f)
        val consumed = when {
            // We are flinging without having dragged the pull refresh (for example a fling inside
            // a list) - don't consume
            distancePulled == 0f -> 0f
            // If the velocity is negative, the fling is upwards, and we don't want to prevent the
            // the list from scrolling
            velocity < 0f -> 0f
            // We are showing the indicator, and the fling is downwards - consume everything
            else -> velocity
        }
        distancePulled = 0f
        return consumed
    }

    internal fun setRefreshing(refreshing: Boolean) {
        if (this.refreshing != refreshing) {
            this.refreshing = refreshing
            distancePulled = 0f
            animateIndicatorTo(if (refreshing) refreshingOffset else 0f)
        }
    }

    internal fun setThreshold(threshold: Float) {
        this.threshold = threshold
    }

    internal fun setRefreshingOffset(refreshingOffset: Float) {
        if (this.refreshingOffset != refreshingOffset) {
            this.refreshingOffset = refreshingOffset
            if (refreshing) animateIndicatorTo(refreshingOffset)
        }
    }

    // Make sure to cancel any existing animations when we launch a new one. We use this instead of
    // Animatable as calling snapTo() on every drag delta has a one frame delay, and some extra
    // overhead of running through the animation pipeline instead of directly mutating the state.
    private val mutatorMutex = MutatorMutex()

    private fun animateIndicatorTo(offset: Float) = animationScope.launch {
        mutatorMutex.mutate {
            animate(initialValue = position, targetValue = offset) { value, _ ->
                position = value
            }
        }
    }

    private fun calculateIndicatorPosition(): Float = when {
        // If drag hasn't gone past the threshold, the position is the adjustedDistancePulled.
        adjustedDistancePulled <= threshold -> adjustedDistancePulled
        else -> {
            // How far beyond the threshold pull has gone, as a percentage of the threshold.
            val overshootPercent = abs(progress) - 1.0f
            // Limit the overshoot to 200%. Linear between 0 and 200.
            val linearTension = overshootPercent.coerceIn(0f, 2f)
            // Non-linear tension. Increases with linearTension, but at a decreasing rate.
            val tensionPercent = linearTension - linearTension.pow(2) / 4
            // The additional offset beyond the threshold.
            val extraOffset = threshold * tensionPercent
            threshold + extraOffset
        }
    }
}

/**
 * Default parameter values for [rememberPullRefreshState].
 */
object PullRefreshDefaults {
    /**
     * If the indicator is below this threshold offset when it is released, a refresh
     * will be triggered.
     */
    val RefreshThreshold = 80.dp

    /**
     * The offset at which the indicator should be rendered whilst a refresh is occurring.
     */
    val RefreshingOffset = 56.dp
}


/**
 * A nested scroll modifier that provides scroll events to [state].
 *
 * Note that this modifier must be added above a scrolling container, such as a lazy column, in
 * order to receive scroll events. For example:
 *
 * @sample androidx.compose.material.samples.PullRefreshSample
 *
 * @param state The [PullRefreshState] associated with this pull-to-refresh component.
 * The state will be updated by this modifier.
 * @param enabled If not enabled, all scroll delta and fling velocity will be ignored.
 */
fun Modifier.nestedScrollForPullRefresh(
    state: PullRefreshState,
    enabled: Boolean = true
) = nestedScrollForPullRefresh(state::onPull, state::onRelease, enabled)

/**
 * A nested scroll modifier that provides [onPull] and [onRelease] callbacks to aid building custom
 * pull refresh components.
 *
 * Note that this modifier must be added above a scrolling container, such as a lazy column, in
 * order to receive scroll events. For example:
 *
 * @sample androidx.compose.material.samples.CustomPullRefreshSample
 *
 * @param onPull Callback for dispatching vertical scroll delta, takes float pullDelta as argument.
 * Positive delta (pulling down) is dispatched only if the child does not consume it (i.e. pulling
 * down despite being at the top of a scrollable component), whereas negative delta (swiping up) is
 * dispatched first (in case it is needed to push the indicator back up), and then the unconsumed
 * delta is passed on to the child. The callback returns how much delta was consumed.
 * @param onRelease Callback for when drag is released, takes float flingVelocity as argument.
 * The callback returns how much velocity was consumed - in most cases this should only consume
 * velocity if pull refresh has been dragged already and the velocity is positive (the fling is
 * downwards), as an upwards fling should typically still scroll a scrollable component beneath the
 * pullRefresh. This is invoked before any remaining velocity is passed to the child.
 * @param enabled If not enabled, all scroll delta and fling velocity will be ignored and neither
 * [onPull] nor [onRelease] will be invoked.
 */
fun Modifier.nestedScrollForPullRefresh(
    onPull: (pullDelta: Float) -> Float,
    onRelease: suspend (flingVelocity: Float) -> Float,
    enabled: Boolean = true
) = Modifier.nestedScroll(PullRefreshNestedScrollConnection(onPull, onRelease, enabled))