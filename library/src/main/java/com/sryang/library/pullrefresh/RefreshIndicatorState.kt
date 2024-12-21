package com.sryang.library.pullrefresh

enum class RefreshIndicatorState(msg: String) {
    Default("Default"),
    PullingDown("PullingDown"),
    ReachedThreshold("ReachedThreshold"),
    Refreshing("Refreshing")
}