package com.sryang.library.pullrefresh

import android.util.Log

class Log {
    companion object {
        val DEBUG = false

        fun d(tag: String, message: String) {
            if (DEBUG)
                Log.d(tag, message)
        }
    }
}