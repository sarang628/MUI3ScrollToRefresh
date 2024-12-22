package com.sryang.library.pullrefresh

/**
 * 리프레시 인디케이터 상태
 */
enum class RefreshIndicatorState {
    /** 기본 */
    Default,

    /** 당김 */
    PullingDown,

    /** 이벤트 요청 도달 */
    ReachedThreshold,

    /** 요청 중 */
    Refreshing
}