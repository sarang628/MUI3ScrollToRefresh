package com.sryang.library.pullrefresh

import android.util.Log
import androidx.compose.animation.core.animate
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow

@Composable
fun rememberPullRefreshState(
    refreshing: Boolean,
    onRefresh: () -> Unit,
    refreshThreshold: Dp = PullRefreshDefaults.RefreshThreshold,
    refreshingOffset: Dp = PullRefreshDefaults.RefreshingOffset,
): PullRefreshState {
    require(refreshThreshold > 0.dp) { "The refresh trigger must be greater than zero!" }

    val scope = rememberCoroutineScope()
    val onRefreshState = rememberUpdatedState(onRefresh)
    val thresholdPx: Float
    val refreshingOffsetPx: Float

    with(LocalDensity.current) {
        thresholdPx = refreshThreshold.toPx()
        refreshingOffsetPx = refreshingOffset.toPx()
    }

    val state = remember(scope) {
        PullRefreshState(scope, onRefreshState, refreshingOffsetPx, thresholdPx)
    }

    SideEffect {
        state.setRefreshing(refreshing)
        state.setThreshold(thresholdPx)
        state.setRefreshingOffset(refreshingOffsetPx)
    }

    return state
}

/**
 * A state object that can be used in conjunction with [pullRefresh] to add pull-to-refresh
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
    val TAG = "__PullRefreshState"

    /**
     * A float representing how far the user has pulled as a percentage of the refreshThreshold.
     *
     * If the component has not been pulled at all, progress is zero. If the pull has reached
     * halfway to the threshold, progress is 0.5f. A value greater than 1 indicates that pull has
     * gone beyond the refreshThreshold - e.g. a value of 2f indicates that the user has pulled to
     * two times the refreshThreshold.
     */
    val progress get() = adjustedDistancePulled / threshold
    private val adjustedDistancePulled by derivedStateOf { distancePulled * DragMultiplier }
    private var refreshing by mutableStateOf(false)
    private var position by mutableFloatStateOf(0f)
    /** 부모가 당겨진 값 */
    private var distancePulled by mutableFloatStateOf(0f)
    private var threshold by mutableFloatStateOf(threshold)
    private var refreshingOffset by mutableFloatStateOf(refreshingOffset)

    /**
     * 당김 이벤트 처리
     *
     * @param pullDelta 입력된 드레그 거리
     *
     * @return 프로그레스바가 표시되어야 하는 높이 (음수 값이 되면 부모화면이 위로 스크롤 되서 발생 안하게 해야 함)
     * */
    internal fun onPull(pullDelta: Float): Float {
        if (refreshing) return 0f // Already refreshing, do nothing.

        /** 계산된 새로운 거리 */
        val newOffset = (distancePulled + pullDelta).coerceAtLeast(0f)

        val dragConsumed = newOffset - distancePulled
        distancePulled = newOffset
        position = calculateIndicatorPosition()

        Log.d(
            TAG,
            "newOffset($newOffset) = distancePulled($distancePulled) + pullDelta($pullDelta) = ${distancePulled + pullDelta}, dragConsumed :${dragConsumed}"
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
 * The distance pulled is multiplied by this value to give us the adjusted distance pulled, which
 * is used in calculating the indicator position (when the adjusted distance pulled is less than
 * the refresh threshold, it is the indicator position, otherwise the indicator position is
 * derived from the progress).
 */
private const val DragMultiplier = 0.5f


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
// TODO(b/244423199): Move pullRefresh into its own material library similar to material-ripple.
fun Modifier.pullRefresh(
    state: PullRefreshState,
    enabled: Boolean = true
) = inspectable(inspectorInfo = debugInspectorInfo {
    name = "pullRefresh"
    properties["state"] = state
    properties["enabled"] = enabled
}) {
    Modifier.pullRefresh(state::onPull, state::onRelease, enabled)
}

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
fun Modifier.pullRefresh(
    onPull: (pullDelta: Float) -> Float,
    onRelease: suspend (flingVelocity: Float) -> Float,
    enabled: Boolean = true
) = inspectable(inspectorInfo = debugInspectorInfo {
    name = "pullRefresh"
    properties["onPull"] = onPull
    properties["onRelease"] = onRelease
    properties["enabled"] = enabled
}) {
    Modifier.nestedScroll(PullRefreshNestedScrollConnection(onPull, onRelease, enabled))
}